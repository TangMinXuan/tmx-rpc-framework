package github.tmx;

import github.tmx.registry.DefaultServiceRegistry;
import github.tmx.transmission.socket.RpcServer;

public class RpcServerExampleMain {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        DefaultServiceRegistry defaultServiceRegistry = new DefaultServiceRegistry();
        // 手动注册
        defaultServiceRegistry.register(helloService);
        RpcServer rpcServer = new RpcServer(defaultServiceRegistry);
        rpcServer.start(9999);
    }
}
