package github.tmx.rpc.core.registry.zookeeper;

import github.tmx.rpc.core.config.ConfigurationEnum;
import github.tmx.rpc.core.config.FrameworkConfiguration;
import github.tmx.rpc.core.extension.ExtensionLoader;
import github.tmx.rpc.core.loadbalance.LoadBalance;
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
    private LoadBalance loadBalance = null;

    public ZkServiceDiscovery() {
        // 实例化时就连接 Zookeeper 注册中心, 防止调用过程需要等待连接 Zk
        zkClient = CuratorUtil.getZkClient();
        String loadBalanceStrategy = FrameworkConfiguration.getProperty(ConfigurationEnum.CLIENT_LOAD_BALANCE);
        loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(loadBalanceStrategy);
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        List<String> providerList = CuratorUtil.getChildrenNodes(zkClient, serviceName);
        if (providerList.size() <= 0) {
            logger.error("找不到服务提供者地址");
            return null;
        }
        String serviceAddress = loadBalance.selectServiceAddress(providerList, serviceName);
        logger.debug("成功找到服务地址:{}", serviceAddress);

        //按照 ip:port 的格式切分 serviceAddress
        return new InetSocketAddress(serviceAddress.split(":")[0], Integer.parseInt(serviceAddress.split(":")[1]));
    }
}
