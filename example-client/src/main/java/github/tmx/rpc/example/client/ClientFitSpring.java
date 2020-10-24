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
public class ClientFitSpring {

    @RpcReference
    HelloService helloService;

    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(ClientFitSpring.class);

        ClientFitSpring clientFitSpring = ctx.getBean(ClientFitSpring.class);

        Hello hello_send = new Hello(1, "Hello, server");
        Hello hello_rec = clientFitSpring.helloService.sayHello(hello_send);

        if (hello_rec != null) {
            System.out.println("hello from client: " + "id: " + hello_rec.getId() + " " + "message: " + hello_rec.getMessage());
        } else {
            System.out.println("RPC调用结果为null");
        }
    }
}
