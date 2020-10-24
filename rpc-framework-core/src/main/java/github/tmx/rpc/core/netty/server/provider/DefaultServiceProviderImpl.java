package github.tmx.rpc.core.netty.server.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultServiceProviderImpl implements ServiceProvider {

    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceProviderImpl.class);

    // key-value: interfaceName-interfaceImplObject
    private static final Map<String, Object> providerMap = new HashMap<>();

    /**
     * 添加服务
     * @param serviceName
     */
    @Override
    public void addProvider(String serviceName, Object service) {
        if (providerMap.containsKey(serviceName)) {
            logger.info("接口: {} 已经添加过了, 不允许重复添加", serviceName);
            return ;
        }
        providerMap.put(serviceName, service);
        logger.info("成功添加接口: {}", serviceName);
    }

    @Override
    public Object getProvider(String serviceName) {
        Object service = providerMap.get(serviceName);
        if (null == service) {
            logger.error("找不到服务: {}", serviceName);
        }
        return service;
    }

    @Override
    public List<String> getAllService() {
        List<String> keysList = new ArrayList<>();
        for (String interfaceName : providerMap.keySet()) {
            keysList.add(interfaceName);
        }
        return keysList;
    }
}
