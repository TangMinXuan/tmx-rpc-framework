package github.tmx.netty.server.provider;

/**
 * 服务端保存和获取接口实现对象
 */
public interface ServiceProvider {

    <T> void addProvider(T service);

    Object getProvider(String serviceName);
}
