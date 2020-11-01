package github.tmx.rpc.core.registry;

import github.tmx.rpc.core.extension.SPI;

import java.net.InetSocketAddress;

/**
 * @author: TangMinXuan
 * @created: 2020/10/22 18:38
 */
@SPI
public interface ServiceDiscovery {

    /**
     * 查找服务
     *
     * @param serviceName 接口名
     * @return 提供服务的 provider 的地址
     */
    InetSocketAddress lookupService(String serviceName);
}
