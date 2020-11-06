package github.tmx.rpc.core.spring.component;

import github.tmx.rpc.core.netty.server.NettyServer;
import github.tmx.rpc.core.spring.annotation.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * 将 @RpcService 标记的 Bean 发布出去
 * @author: TangMinXuan
 * @created: 2020/10/21 18:14
 */
@Component
public class RpcServiceProcessor implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(RpcServiceProcessor.class);

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            logger.debug("[{}] 被标记为 @RpcService", bean.getClass().getName());
            NettyServer.getInstance().registerService(beanName);
        }
        return bean;
    }
}
