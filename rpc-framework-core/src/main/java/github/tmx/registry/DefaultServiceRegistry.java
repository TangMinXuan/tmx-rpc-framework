package github.tmx.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DefaultServiceRegistry implements ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceRegistry.class);

    private static final Map<String, Object> serviceMap = new HashMap<>();

    /**
     * 注册服务
     * 为何使用 synchronized ？
     * 考虑 2 个 provider 同时进入这个方法，且都是注册对同一个接口的实现
     *
     * @param service
     * @param <T>
     */
    @Override
    public synchronized <T> void register(T service) {
        Class[] interfaces = service.getClass().getInterfaces();
        if (interfaces.length == 0) {
            logger.error("服务没有实现任何接口");
        }
        for (Class i : interfaces) {
            if (serviceMap.containsKey(i.getCanonicalName())) {
                logger.error("接口: {} 已经注册过了,不允许重复注册", i.getCanonicalName());
                continue;
            }
            serviceMap.put(i.getCanonicalName(), service);
            logger.info("成功添加接口: {}", i.getCanonicalName());
        }
    }

    @Override
    public Object getService(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (null == service) {
            logger.error("找不到服务: {}", serviceName);
        }
        return service;
    }
}
