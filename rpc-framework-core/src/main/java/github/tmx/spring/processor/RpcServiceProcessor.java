package github.tmx.spring.processor;

import github.tmx.netty.server.NettyServer;
import github.tmx.spring.annotion.RpcService;
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

    private NettyServer nettyServer = null;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            logger.info("[{}] 被标记为  [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            if (nettyServer == null) {
                nettyServer = NettyServer.getInstance();
            }
            nettyServer.publishService(bean);
        }
        return bean;
    }
}
