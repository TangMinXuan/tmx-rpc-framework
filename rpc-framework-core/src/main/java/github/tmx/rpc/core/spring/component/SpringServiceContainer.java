package github.tmx.rpc.core.spring.component;

import github.tmx.rpc.core.container.ServiceContainer;
import github.tmx.rpc.core.spring.BeanNameUtil;
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
 * Spring 服务容器的实现
 * @author: TangMinXuan
 * @created: 2020/10/31 16:00
 */
@Component
public class SpringServiceContainer implements ServiceContainer, ApplicationContextAware, BeanDefinitionRegistryPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(SpringServiceContainer.class);

    private static ApplicationContext applicationContext;
    private static BeanDefinitionRegistry registry;

    @Override
    public void addProvider(Object service) {
        String beanName = BeanNameUtil.getBeanName(service);
        if (applicationContext.containsBean(beanName)) {
            return ;
        }
        RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(service.getClass());
        registry.registerBeanDefinition(beanName, rootBeanDefinition);
    }

    @Override
    public Object getProvider(String serviceName, String group, String version) {
        String beanName = BeanNameUtil.getBeanName(serviceName, group, version);
        return applicationContext.getBean(beanName);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringServiceContainer.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        SpringServiceContainer.registry = registry;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
