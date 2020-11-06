package github.tmx.rpc.core.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: TangMinXuan
 */
public class DefaultServiceContainer implements ServiceContainer {

    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceContainer.class);

    /**
     * key: 接口名
     * value: 接口实现类对象
     */
    private static Map<String, Object> providerMap = new HashMap<>();

    @Override
    public void addProvider(Object service) {
        String serviceName = null;
        if (providerMap.containsKey(serviceName)) {
            logger.info("接口: {} 已经添加过了, 不允许重复添加", serviceName);
            return ;
        }
        providerMap.put(serviceName, service);
        logger.info("成功添加接口: {}", serviceName);
    }

    @Override
    public Object getProvider(String serviceName, String group, String version) {
        Object service = providerMap.get(serviceName);
        if (null == service) {
            logger.error("找不到服务: {}", serviceName);
        }
        return service;
    }
}
