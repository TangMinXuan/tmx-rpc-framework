package github.tmx;

import github.tmx.netty.client.NettyRpcClient;
import github.tmx.netty.client.NettyRpcClientProxy;
import github.tmx.netty.client.RpcClient;

/**
 * @author: TangMinXuan
 * @created: 2020/10/05 11:06
 */
public class NioRpcClientMain {

    public static void main(String[] args) {
        RpcClient rpcClient = new NettyRpcClient();
        NettyRpcClientProxy nettyRpcClientProxy = new NettyRpcClientProxy(rpcClient);
        HelloService helloService = nettyRpcClientProxy.getProxyInstance(HelloService.class);

        Hello hello_send = new Hello(1, "Hello, server");
        Hello hello_rec = helloService.sayHello(hello_send);
        System.out.println("hello from client: " +
                "id: " + hello_rec.getId() + " " +
                "message: " + hello_rec.getMessage());
    }
}
