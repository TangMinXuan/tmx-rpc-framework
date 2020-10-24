package github.tmx.rpc.core.registry;

import java.net.InetSocketAddress;

/**
 * @author: TangMinXuan
 * @created: 2020/10/22 18:38
 */
public interface ServiceDiscovery {

    /**
     * 查找服务
     *
     * @param interfaceName 接口名
     * @return 提供服务的 provider 的地址
     */
    InetSocketAddress lookupService(String interfaceName);
}
