package github.tmx.rpc.example.client;

import github.tmx.example.api.Hello;
import github.tmx.example.api.HelloService;
import github.tmx.rpc.core.spring.annotion.EnableRPC;
import github.tmx.rpc.core.spring.annotion.RpcReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author: TangMinXuan
 * @created: 2020/10/21 16:45
 */
@EnableRPC
public class RpcClientApplication {

    @RpcReference
    HelloService helloService;

    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(RpcClientApplication.class);

        RpcClientApplication clientFitSpring = ctx.getBean(RpcClientApplication.class);

        Hello helloSend = new Hello(1, "Hello, server");
        Hello helloRec = clientFitSpring.helloService.sayHello(helloSend);

        if (helloRec != null) {
            System.out.println("hello from client: " + "id: " + helloRec.getId() + " " + "message: " + helloRec.getMessage());
        } else {
            System.out.println("RPC调用结果为null");
        }
    }
}
