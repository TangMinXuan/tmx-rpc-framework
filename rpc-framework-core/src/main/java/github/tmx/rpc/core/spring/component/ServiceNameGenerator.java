package github.tmx.rpc.core.spring.component;

import github.tmx.rpc.core.spring.BeanNameUtil;
import github.tmx.rpc.core.spring.annotation.RpcService;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;

/**
 * @author: TangMinXuan
 * @created: 2020/11/01 10:52
 */
public class ServiceNameGenerator extends AnnotationBeanNameGenerator {

    /**
     * 为注入 Spring 的 RpcService 修改为: 接口全限定名 + [group] + [version]
     * 默认 beanName 为: 实现类简名(首字母小写)
     * @param definition
     * @return
     */
    @Override
    protected String buildDefaultBeanName(BeanDefinition definition) {
        String beanClassName = definition.getBeanClassName();
        Class<?> clazz = null;
        try {
            clazz = Class.forName(beanClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (clazz.isAnnotationPresent(RpcService.class) && clazz.getInterfaces().length > 0) {
            RpcService rpcService = clazz.getAnnotation(RpcService.class);
            String interfaceName = clazz.getInterfaces()[0].getCanonicalName();
            return BeanNameUtil.getBeanName(interfaceName, rpcService.group(), rpcService.version());
        }

        return super.buildDefaultBeanName(definition);
    }
}
