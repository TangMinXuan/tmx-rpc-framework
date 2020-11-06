package github.tmx.rpc.core.container;

import github.tmx.rpc.core.extension.SPI;

/**
 * 服务端保存和获取接口实现对象
 * @author: TangMinXuan
 */
@SPI
public interface ServiceContainer {

    /**
     * 添加 实现类 进入 容器
     * @param service
     */
    void addProvider(Object service);

    /**
     * 从 容器 获取对应的 实现类
     * @param serviceName
     * @param group
     * @param version
     * @return
     */
    Object getProvider(String serviceName, String group, String version);
}
