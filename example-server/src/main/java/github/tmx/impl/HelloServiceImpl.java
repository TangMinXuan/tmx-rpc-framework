package github.tmx.impl;

import github.tmx.Hello;
import github.tmx.HelloService;
import github.tmx.spring.annotion.RpcService;

@RpcService
public class HelloServiceImpl implements HelloService {

    @Override
    public Hello sayHello(Hello hello){
        System.out.println("hello from client: " +
                "id: " + hello.getId() + " " +
                "message: " + hello.getMessage());
        Hello hello_return = new Hello(hello.getId() + 1, "Hello, client");
        return hello_return;
    }
}
