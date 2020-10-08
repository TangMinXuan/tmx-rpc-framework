package github.tmx;

import github.tmx.transmission.RpcClientProxy;

public class RpcClientExampleMain {

    public static void main(String[] args) {
        RpcClientProxy rpcClientProxy = new RpcClientProxy("127.0.0.1", 9999);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String res = helloService.sayHello();
        System.out.println("res = " + res);
    }
}
