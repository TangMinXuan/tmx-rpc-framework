package github.tmx;

import github.tmx.client.RpcClient;
import github.tmx.client.RpcClientProxy;
import github.tmx.client.netty.NettyRpcClient;
import github.tmx.client.netty.NettyRpcClientProxy;

/**
 * @author: TangMinXuan
 * @created: 2020/10/05 11:06
 */
public class NioRpcClientMain {

    public static void main(String[] args) {
        RpcClient rpcClient = new NettyRpcClient("127.0.0.1", 9999);
        NettyRpcClientProxy nettyRpcClientProxy = new NettyRpcClientProxy(rpcClient);
        HelloService helloService = nettyRpcClientProxy.getProxyInstance(HelloService.class);
        String hello = helloService.sayHello();
        System.out.println(hello);
    }
}
