package github.tmx;

import github.tmx.registry.DefaultServiceRegistry;
import github.tmx.transmission.netty.server.NettyRpcServer;

/**
 * @author: TangMinXuan
 * @created: 2020/10/05 11:05
 */
public class NioRpcServerMain {

    public static void main(String[] args) {
        HelloServiceImpl helloService = new HelloServiceImpl();
        DefaultServiceRegistry defaultServiceRegistry = new DefaultServiceRegistry();
        // 手动注册
        defaultServiceRegistry.register(helloService);
        NettyRpcServer socketRpcServer = new NettyRpcServer(9999);
        socketRpcServer.startUp();
    }
}
