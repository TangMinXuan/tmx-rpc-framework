package github.tmx.registry;

import java.net.InetSocketAddress;

/**
 * 服务注册中心接口
 *
 * @author: TangMinXuan
 * @created: 2020/10/13 15:06
 */
public interface ServiceRegistry {

    /**
     * 向 Zookeeper 注册服务
     * 目前暂不支持一个实现类实现多个接口的情况, 将来升级为扫描注解注册时会改进
     *
     * @param interfaceName 接口名
     * @param inetSocketAddress 提供服务的 provider 的地址
     */
    void registerService(String interfaceName, InetSocketAddress inetSocketAddress);

    /**
     * 查找服务
     *
     * @param interfaceName 接口名
     * @return 提供服务的 provider 的地址
     */
    InetSocketAddress lookupService(String interfaceName);
}
