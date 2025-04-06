package github.tmx.rpc.core.registry.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import github.tmx.rpc.core.config.ConfigurationEnum;
import github.tmx.rpc.core.config.FrameworkConfiguration;
import github.tmx.rpc.core.registry.ServiceRegistry;
import github.tmx.rpc.core.registry.zookeeper.CuratorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class NacosRegistry implements ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(NacosRegistry.class);

    private static Set<String> registrySet = new HashSet<>();

    private String ip;

    private Integer port;

    public NacosRegistry() {
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
            port = Integer.valueOf(FrameworkConfiguration.getProperty(ConfigurationEnum.SERVER_PORT));
        } catch (UnknownHostException e) {
            logger.error("获取服务器所在地址发生错误: ", e);
        }
    }

    @Override
    public void registerService(String serviceName) {
        String finalServiceName = new StringBuilder(CuratorUtil.ROOT_PATH)
                .append("/").append(serviceName).toString();
        if (registrySet.contains(serviceName)) {
            logger.info("接口: {} 已经注册过了, 不允许重复注册", serviceName);
            return;
        }
        registrySet.add(finalServiceName);
        try {
            NacosClient.getNacosService().registerInstance(finalServiceName, ip, port);
        } catch (NacosException e) {
            logger.error("向Nacos注册服务时发生错误: ", e);
        }
        logger.debug("成功创建节点: {}", finalServiceName);
    }

    @Override
    public void cancelService() {
        for (String finalServiceName : registrySet) {
            try {
                NacosClient.getNacosService().deregisterInstance(finalServiceName, ip, port);
            } catch (NacosException e) {
                logger.error("向Nacos下线服务时发生错误: ", e);
            }
            logger.debug("成功删除: {}", finalServiceName);
        }
        registrySet = new HashSet<>();
    }
}
