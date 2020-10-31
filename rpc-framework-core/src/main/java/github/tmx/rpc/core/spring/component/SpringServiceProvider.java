package github.tmx.rpc.core.spring.component;

import github.tmx.rpc.core.provider.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author: TangMinXuan
 * @created: 2020/10/31 16:00
 */
@Component
public class SpringServiceProvider implements ServiceProvider, ApplicationContextAware, BeanDefinitionRegistryPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(SpringServiceProvider.class);

    private static ApplicationContext applicationContext;
    private static BeanDefinitionRegistry registry;

    @Override
    public void addProvider(String serviceName, Object service) {
        if (getProvider(serviceName) != null) {
            return ;
        }
        RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(service.getClass());
        registry.registerBeanDefinition(service.getClass().getCanonicalName(), rootBeanDefinition);
    }

    @Override
    public Object getProvider(String serviceName) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(serviceName);
        } catch (ClassNotFoundException e) {
            logger.error("由接口名反射接口Class对象时, 无法找到接口类: {}", e);
        }
        String[] serviceImplNameArray = applicationContext.getBeanNamesForType(clazz);
        for (String serviceImplName : serviceImplNameArray) {
            if (applicationContext.containsBean(serviceImplName)) {
                return applicationContext.getBean(serviceImplName);
            }
        }
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringServiceProvider.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        SpringServiceProvider.registry = registry;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
