package github.tmx.rpc.example.spring.consumer;

import github.tmx.rpc.core.spring.annotation.EnableRPC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author: TangMinXuan
 * @created: 2020/10/24 10:14
 */
@SpringBootApplication
@EnableRPC
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}
