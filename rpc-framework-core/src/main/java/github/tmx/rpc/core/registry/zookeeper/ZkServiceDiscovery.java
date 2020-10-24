package github.tmx.rpc.core.registry.zookeeper;

import github.tmx.rpc.core.registry.ServiceDiscovery;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author: TangMinXuan
 * @created: 2020/10/22 18:41
 */
public class ZkServiceDiscovery implements ServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(ZkServiceDiscovery.class);

    private CuratorFramework zkClient = null;

    /**
     * 实例化时就连接 Zookeeper 注册中心, 防止调用过程需要等待连接 Zk
     */
    public ZkServiceDiscovery() {
        zkClient = CuratorUtil.getZkClient();
    }

    @Override
    public InetSocketAddress lookupService(String interfaceName) {
        List<String> providerList = CuratorUtil.getChildrenNodes(zkClient, interfaceName);
        if (providerList.size() <= 0) {
            logger.info("找不到服务提供者地址");
            return null;
        }
        String serviceAddress = providerList.get(0);
        logger.info("成功找到服务地址:{}", serviceAddress);

        //按照 ip:port 的格式切分 serviceAddress
        return new InetSocketAddress(serviceAddress.split(":")[0], Integer.parseInt(serviceAddress.split(":")[1]));
    }
}
