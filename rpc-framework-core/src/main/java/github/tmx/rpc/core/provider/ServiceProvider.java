package github.tmx.rpc.core.provider;

import github.tmx.rpc.core.extension.SPI;

/**
 * 服务端保存和获取接口实现对象
 */
@SPI
public interface ServiceProvider {

    void addProvider(Object service);

    Object getProvider(String serviceName, String group, String version);
}
