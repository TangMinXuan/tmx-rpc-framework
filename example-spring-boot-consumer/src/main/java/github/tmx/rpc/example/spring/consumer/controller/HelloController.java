package github.tmx.rpc.example.spring.consumer.controller;

import github.tmx.example.api.Hello;
import github.tmx.example.api.HelloService;
import github.tmx.rpc.core.spring.annotation.RpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: TangMinXuan
 * @created: 2020/10/24 10:18
 */
@RestController
@RequestMapping("/rpc")
public class HelloController {

    @RpcReference
    HelloService helloService;

    @GetMapping("/hello")
    public Hello hello() {
        Hello hello = new Hello(1, "i'm spring boot");
        return helloService.sayHello(hello);
    }
}
