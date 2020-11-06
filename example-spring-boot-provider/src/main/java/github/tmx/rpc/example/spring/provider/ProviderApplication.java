package github.tmx.rpc.example.spring.provider;

import github.tmx.rpc.core.spring.annotation.EnableRPC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author: TangMinXuan
 * @created: 2020/10/24 16:12
 */
@SpringBootApplication
@EnableRPC
public class ProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}
