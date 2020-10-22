package github.tmx.spring;

import github.tmx.netty.client.NettyClient;
import github.tmx.netty.client.NettyRpcClientProxy;
import github.tmx.netty.client.RpcClient;
import github.tmx.spring.annotion.RpcReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: TangMinXuan
 * @created: 2020/10/21 09:04
 */
public class ReferenceAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter implements
        MergedBeanDefinitionPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ReferenceAnnotationBeanPostProcessor.class);

    // 元数据缓存. 在这里, 对相同接口的注入, 第一次是去 build 元数据, 第二次直接走缓存
    private final Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);

    // 这个类支持处理的注解放在这里边 e: @RpcReference
    private final Set<Class<? extends Annotation>> annotationTypes = new LinkedHashSet<>(1);

    private RpcClient rpcClient;

    /**
     * 构造方法
     */
    public ReferenceAnnotationBeanPostProcessor() {
        this.annotationTypes.add(RpcReference.class);
        this.rpcClient = new NettyClient();
    }


    /**
     * 实例化 Bean 之后, 填充 Bean 对象之前, 会回调这个方法
     * 这里的 Bean 指的是, SpringMain, 而不是 HiService
     * @param beanDefinition
     * @param beanType
     * @param beanName
     */
    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        // annotation 依赖注入：查找 bean 上配置的依赖注入注解配置，并缓存起来
        InjectionMetadata metadata = findAutowiringMetadata(beanName, beanType, null);
        metadata.checkConfigMembers(beanDefinition);
    }

    /**
     * 完成依赖注入
     * @param pvs
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        InjectionMetadata metadata = findAutowiringMetadata(beanName, bean.getClass(), pvs);
        try {
            metadata.inject(bean, beanName, pvs);
        } catch (BeanCreationException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "依赖注入时发生异常", ex);
        }
        return pvs;
    }

    /**
     * 尝试返回自动注入的元数据, 先在缓存找, 找不到就去 build 一个
     * @param beanName
     * @param clazz
     * @param pvs
     * @return
     */
    private InjectionMetadata findAutowiringMetadata(String beanName, Class<?> clazz, @Nullable PropertyValues pvs) {
        // 获得类名
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
        // 用类名去缓存中找元数据
        InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz)) {
                    if (metadata != null) {
                        metadata.clear(pvs);
                    }
                    // 构建 autowire 的元数据，包括 field 和 method
                    metadata = buildAutowiringMetadata(clazz);
                    this.injectionMetadataCache.put(cacheKey, metadata);
                }
            }
        }
        return metadata;
    }

    /**
     * 为指定类 build 一个元数据给他
     * @param clazz
     * @return
     */
    private InjectionMetadata buildAutowiringMetadata(final Class<?> clazz) {

        List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
        Class<?> targetClass = clazz;

        do {
            final List<InjectionMetadata.InjectedElement> currElements = new ArrayList<>();

            ReflectionUtils.doWithLocalFields(targetClass, field -> {
                MergedAnnotation<?> ann = findReferenceAnnotation(field);
                if (ann != null) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        if (logger.isInfoEnabled()) {
                            logger.info("@RpcReference 不支持注入 static 类型: " + field);
                        }
                        return;
                    }
                    AnnotationAttributes annotationAttributes = ann.asMap(mergedAnnotation -> new AnnotationAttributes(mergedAnnotation.getType()));
                    currElements.add(new ReferenceFieldElement(field));
                }
            });

            elements.addAll(0, currElements);
            targetClass = targetClass.getSuperclass();
        }
        while (targetClass != null && targetClass != Object.class);

        return InjectionMetadata.forElements(elements, clazz);
    }

    /**
     * 一个 field 上可能存在很多注解, 返回 field 上我们支持的那个注解(@RpcReference)
     * @param ao
     * @return
     */
    @Nullable
    private MergedAnnotation<?> findReferenceAnnotation(AccessibleObject ao) {
        MergedAnnotations annotations = MergedAnnotations.from(ao);
        for (Class<? extends Annotation> type : this.annotationTypes) {
            MergedAnnotation<?> annotation = annotations.get(type);
            if (annotation.isPresent()) {
                return annotation;
            }
        }
        return null;
    }

    /**
     * 在这个类的 inject() 方法中实现具体的实例化逻辑
     */
    private class ReferenceFieldElement extends InjectionMetadata.InjectedElement {

        protected ReferenceFieldElement(Member member) {
            super(member, null);
        }

        @Override
        protected void inject(Object target, String requestingBeanName, PropertyValues pvs) throws Throwable {
            Field field = (Field) this.member;
            NettyRpcClientProxy nettyRpcClientProxy = new NettyRpcClientProxy(rpcClient);
            Object proxyObject = nettyRpcClientProxy.getProxyInstance(field.getType());
            if (proxyObject != null) {
                ReflectionUtils.makeAccessible(field);
                field.set(target, proxyObject);
            }
        }
    }
}
