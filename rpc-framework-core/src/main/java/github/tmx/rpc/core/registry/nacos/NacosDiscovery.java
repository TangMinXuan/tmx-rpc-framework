package github.tmx.rpc.core.registry.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import github.tmx.rpc.core.config.ConfigurationEnum;
import github.tmx.rpc.core.config.FrameworkConfiguration;
import github.tmx.rpc.core.extension.ExtensionLoader;
import github.tmx.rpc.core.loadbalance.LoadBalance;
import github.tmx.rpc.core.registry.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class NacosDiscovery implements ServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(NacosDiscovery.class);

    private LoadBalance loadBalance = null;

    public NacosDiscovery() {
        String loadBalanceStrategy = FrameworkConfiguration.getProperty(ConfigurationEnum.CLIENT_LOAD_BALANCE);
        loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(loadBalanceStrategy);
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        List<Instance> providerList = null;
        try {
            providerList = NacosClient.getNacosService().getAllInstances(serviceName);
        } catch (NacosException e) {
            logger.error("向Nacos获取服务时发生错误: ", e);
        }
        if (null == providerList || providerList.size() == 0) {
            logger.error("找不到服务提供者地址");
            return null;
        }
        String serviceAddress = loadBalance.selectServiceAddress(transform(providerList), serviceName);
        logger.debug("成功找到服务地址:{}", serviceAddress);

        //按照 ip:port 的格式切分 serviceAddress
        return new InetSocketAddress(serviceAddress.split(":")[0], Integer.parseInt(serviceAddress.split(":")[1]));
    }

    private List<String> transform(List<Instance> providerList) {
        List<String> providerStringList = new ArrayList<>();
        for (Instance ins : providerList) {
            StringBuilder builder = new StringBuilder()
                    .append(ins.getIp()).append(":").append(ins.getPort());
            providerStringList.add(builder.toString());
        }
        return providerStringList;
    }
}
