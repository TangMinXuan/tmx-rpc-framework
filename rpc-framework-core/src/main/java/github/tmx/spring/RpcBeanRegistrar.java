package github.tmx.spring;

import github.tmx.spring.annotion.EnableRPC;
import github.tmx.spring.annotion.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;

/**
 * @author: TangMinXuan
 * @created: 2020/10/21 15:12
 */
public class RpcBeanRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private static final Logger logger = LoggerFactory.getLogger(RpcBeanRegistrar.class);

    private static final String SPRING_BEAN_BASE_PACKAGE = "github.tmx.spring";
    private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackage";
    public static final String PROCESSOR_BEAN_NAME = "referenceAnnotationBeanPostProcessor";
    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        // 获得 @EnableRPC 注解里边所有的属性
        AnnotationAttributes rpcScanAnnotationAttributes = AnnotationAttributes.
                fromMap(annotationMetadata.getAnnotationAttributes(EnableRPC.class.getName()));
        String[] rpcScanBasePackages = new String[0];
        if (rpcScanAnnotationAttributes != null) {
            // 获得 @EnableRPC 注解中 basePackage 这个字符串数组
            rpcScanBasePackages = rpcScanAnnotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
        }
        if (rpcScanBasePackages.length == 0) {
            rpcScanBasePackages = new String[]{((StandardAnnotationMetadata) annotationMetadata).getIntrospectedClass().getPackage().getName()};
        }
        // Scan the RpcService annotation
        RpcBeanScanner rpcServiceScanner = new RpcBeanScanner(beanDefinitionRegistry, RpcService.class);
        // Scan the Component annotation
        RpcBeanScanner springBeanScanner = new RpcBeanScanner(beanDefinitionRegistry, Component.class);
        if (resourceLoader != null) {
            rpcServiceScanner.setResourceLoader(resourceLoader);
            springBeanScanner.setResourceLoader(resourceLoader);
        }
        int springBeanAmount = springBeanScanner.scan(SPRING_BEAN_BASE_PACKAGE);
        logger.info("springBeanScanner扫描的数量 [{}]", springBeanAmount);
        int scanCount = rpcServiceScanner.scan(rpcScanBasePackages);
        logger.info("rpcServiceScanner扫描的数量 [{}]", scanCount);

        beanDefinitionRegistry.registerBeanDefinition(PROCESSOR_BEAN_NAME,
                new RootBeanDefinition(ReferenceAnnotationBeanPostProcessor.class));
    }
}
