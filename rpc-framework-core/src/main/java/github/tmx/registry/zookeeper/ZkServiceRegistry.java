package github.tmx.registry.zookeeper;

import github.tmx.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: TangMinXuan
 * @created: 2020/10/13 10:29
 */
public class ZkServiceRegistry implements ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ZkServiceRegistry.class);

    private static final Set<String> registrySet = new HashSet<>();

    @Override
    public void registerService(String serviceName, InetSocketAddress inetSocketAddress) {
        if (registrySet.contains(serviceName)) {
            logger.info("接口: {} 已经注册过了, 不允许重复注册", serviceName);
        }
        // 根节点下注册子节点：接口名
        StringBuilder servicePath = new StringBuilder(CuratorUtil.ZK_REGISTER_ROOT_PATH).append("/").append(serviceName);

        // 服务子节点下注册子节点：服务地址
        // 示例: /tmx-rpc/tmx.github.HelloService/127.0.0.1:9999
        servicePath.append(inetSocketAddress.toString());
        CuratorUtil.createEphemeralNode(CuratorUtil.getZkClient(), servicePath.toString());
        logger.info("成功创建节点: {}", servicePath);
    }

    @Override
    public void cancelService(String interfaceName, InetSocketAddress inetSocketAddress) {
        StringBuilder servicePath = new StringBuilder(CuratorUtil.ZK_REGISTER_ROOT_PATH)
                .append("/")
                .append(interfaceName)
                .append(inetSocketAddress.toString());
        CuratorUtil.deleteEphemeralNode(CuratorUtil.getZkClient(), servicePath.toString());
        logger.info("成功删除节点: {}", servicePath);
    }
}
