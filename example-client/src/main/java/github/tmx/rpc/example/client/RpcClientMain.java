package github.tmx.rpc.example.client;

import github.tmx.example.api.Hello;
import github.tmx.example.api.HelloService;
import github.tmx.rpc.core.netty.client.NettyRpcClientProxy;

/**
 * @author: TangMinXuan
 * @created: 2020/10/05 11:06
 */
public class RpcClientMain {

    public static void main(String[] args) {
        NettyRpcClientProxy proxy = new NettyRpcClientProxy();
        HelloService helloService = proxy.getProxyInstance(HelloService.class);
        Hello hello_send = new Hello(1, "Hello, server");
        Hello hello_rec = helloService.sayHello(hello_send);
        if (hello_rec != null) {
            System.out.println("hello from client: " + "id: " + hello_rec.getId() + " " + "message: " + hello_rec.getMessage());
        } else {
            System.out.println("RPC调用结果为null");
        }
    }
}
