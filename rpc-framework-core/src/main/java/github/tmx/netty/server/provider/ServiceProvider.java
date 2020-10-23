package github.tmx.netty.server.provider;

import java.util.List;

/**
 * 服务端保存和获取接口实现对象
 */
public interface ServiceProvider {

    void addProvider(String serviceName, Object service);

    Object getProvider(String serviceName);

    List<String> getAllService();
}
