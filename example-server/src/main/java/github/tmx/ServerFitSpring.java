package github.tmx;

import github.tmx.netty.server.NettyServer;
import github.tmx.spring.annotion.EnableRPC;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author: TangMinXuan
 * @created: 2020/10/21 19:08
 */
@EnableRPC(basePackage = {"github.tmx"})
public class ServerFitSpring {

    public static void main(String[] args) {
        NettyServer nettyRpcServer = new NettyServer("127.0.0.1", 9999);
        ApplicationContext ctx = new AnnotationConfigApplicationContext(ServerFitSpring.class);
        nettyRpcServer.startUp();
    }
}
