package github.tmx.rpc.example.server;

import github.tmx.example.api.HelloService;
import github.tmx.rpc.core.netty.server.NettyServer;
import github.tmx.rpc.example.server.impl.HelloServiceImpl;

import java.io.IOException;

/**
 * @author: TangMinXuan
 * @created: 2020/10/05 11:05
 */
public class RpcServerMain {

    public static void main(String[] args) throws IOException {
        NettyServer nettyServer = NettyServer.getInstance();
        HelloService helloService = new HelloServiceImpl();
        nettyServer.publishService(helloService);
        System.in.read();
    }
}
