package github.tmx;

import github.tmx.netty.server.NettyServer;

/**
 * @author: TangMinXuan
 * @created: 2020/10/05 11:05
 */
public class NioRpcServerMain {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        NettyServer nettyRpcServer = new NettyServer("127.0.0.1", 9999);
        nettyRpcServer.startUp(helloService, HelloService.class);
    }
}
