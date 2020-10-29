package github.tmx.rpc.core.registry;

import github.tmx.rpc.core.extension.SPI;

/**
 * 服务注册中心接口
 *
 * @author: TangMinXuan
 * @created: 2020/10/13 15:06
 */
@SPI
public interface ServiceRegistry {

    /**
     * 向注册中心注册服务
     * @param serviceName 接口名
     */
    void registerService(String serviceName);

    /**
     * 注销所有服务
     */
    void cancelService();
}
