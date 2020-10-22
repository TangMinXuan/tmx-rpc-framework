package github.tmx.spring;

import github.tmx.netty.server.provider.DefaultServiceProviderImpl;
import github.tmx.netty.server.provider.ServiceProvider;
import github.tmx.spring.annotion.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @author: TangMinXuan
 * @created: 2020/10/21 18:14
 */
@Component
public class RpcServicePublishProcessor implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(RpcServicePublishProcessor.class);

    private ServiceProvider serviceProvider;

    public RpcServicePublishProcessor() {
        serviceProvider = new DefaultServiceProviderImpl();
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            logger.info("[{}] 被标记为  [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            serviceProvider.publishService(bean);
        }
        return bean;
    }
}
