package github.tmx;

import github.tmx.netty.server.NettyServer;
import github.tmx.spring.annotion.EnableRPC;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author: TangMinXuan
 * @created: 2020/10/21 19:08
 */
@EnableRPC(basePackage = {"github.tmx.impl"})
public class ServerFitSpring {

    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(ServerFitSpring.class);
        NettyServer nettyServer = NettyServer.getInstance();
        nettyServer.startUp();
    }
}
