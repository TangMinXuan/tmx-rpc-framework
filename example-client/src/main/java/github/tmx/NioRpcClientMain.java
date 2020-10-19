package github.tmx;

import github.tmx.netty.client.NettyClient;
import github.tmx.netty.client.NettyRpcClientProxy;
import github.tmx.netty.client.RpcClient;

/**
 * @author: TangMinXuan
 * @created: 2020/10/05 11:06
 */
public class NioRpcClientMain {

    public static void main(String[] args) {
        RpcClient rpcClient = new NettyClient();
        NettyRpcClientProxy nettyRpcClientProxy = new NettyRpcClientProxy(rpcClient);
        HelloService helloService = nettyRpcClientProxy.getProxyInstance(HelloService.class);

        Hello hello_send = new Hello(1, "Hello, server");
        Hello hello_rec = helloService.sayHello(hello_send);
        if (hello_rec == null) {
            System.out.println("RPC调用结果为null");
        }
        System.out.println("hello from client: " +
                "id: " + hello_rec.getId() + " " +
                "message: " + hello_rec.getMessage());
    }
}
