package github.tmx.rpc.core.registry.zookeeper;

import github.tmx.rpc.core.common.config.RpcConfig;
import github.tmx.rpc.core.common.enumeration.RpcPropertyEnum;
import github.tmx.rpc.core.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: TangMinXuan
 * @created: 2020/10/13 10:29
 */
public class ZkServiceRegistry implements ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ZkServiceRegistry.class);

    private static final Set<String> registrySet = new HashSet<>();

    private String address;

    public ZkServiceRegistry() {
        try {
            address = InetAddress.getLocalHost().getHostAddress() + ":"
                    + RpcConfig.getProperty(RpcPropertyEnum.SERVER_PORT);
        } catch (UnknownHostException e) {
            logger.error("获取服务器所在地址发生错误: ", e);
        }
    }

    @Override
    public void registerService(String serviceName) {
        // 示例: /tmx-rpc/tmx.github.HelloService/127.0.0.1:9999
        StringBuilder servicePath = new StringBuilder(CuratorUtil.ROOT_PATH)
                .append("/").append(serviceName)
                .append("/").append(address);
        if (registrySet.contains(serviceName)) {
            logger.info("接口: {} 已经注册过了, 不允许重复注册", serviceName);
            return ;
        }
        registrySet.add(servicePath.toString());
        CuratorUtil.createEphemeralNode(CuratorUtil.getZkClient(), servicePath.toString());
        logger.info("成功创建节点: {}", servicePath);
    }

    @Override
    public void cancelService() {
        for (String servicePath : registrySet) {
            CuratorUtil.deleteEphemeralNode(CuratorUtil.getZkClient(), servicePath);
            logger.info("成功删除: {}", servicePath);
        }
    }
}
