package github.tmx.registry;

import github.tmx.common.utils.CuratorUtil;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @author: TangMinXuan
 * @created: 2020/10/13 10:29
 */
public class ZkServiceRegistry implements ServiceRegistry{

    private static final Logger logger = LoggerFactory.getLogger(ZkServiceRegistry.class);
    
    private final CuratorFramework zkClient;

    public ZkServiceRegistry() {
        zkClient = CuratorUtil.getZkClient();
        zkClient.start();
    }

    @Override
    public void registerService(String interfaceName, InetSocketAddress inetSocketAddress) {
        // 根节点下注册子节点：接口名
        StringBuilder servicePath = new StringBuilder(CuratorUtil.ZK_REGISTER_ROOT_PATH).append("/").append(interfaceName);

        // 服务子节点下注册子节点：服务地址
        // 示例: /tmx-rpc/tmx.github.HelloService/127.0.0.1:9999
        servicePath.append(inetSocketAddress.toString());
        CuratorUtil.createEphemeralNode(zkClient, servicePath.toString());
        logger.info("节点创建成功，节点为:{}", servicePath);
    }

    @Override
    public InetSocketAddress lookupService(String interfaceName) {
        // 默认选择 providerList 中的第一个地址
        // TODO(tmx): 尝试模仿 Dubbo 的负载均衡
        String serviceAddress = CuratorUtil.getChildrenNodes(zkClient, interfaceName).get(0);

        logger.info("成功找到服务地址:{}", serviceAddress);

        //按照 ip:port 的格式切分 serviceAddress
        return new InetSocketAddress(serviceAddress.split(":")[0], Integer.parseInt(serviceAddress.split(":")[1]));
    }
}
