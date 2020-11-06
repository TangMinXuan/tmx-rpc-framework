package github.tmx.rpc.example.server;

import github.tmx.rpc.core.spring.annotation.EnableRPC;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

/**
 * @author: TangMinXuan
 * @created: 2020/10/21 19:08
 */
@EnableRPC
public class RpcServerApplication {

    public static void main(String[] args) throws IOException {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(RpcServerApplication.class);
        // 主动阻塞, 等待客户端连接(在 Spring Boot 中无需主动阻塞)
        System.in.read();
    }
}
