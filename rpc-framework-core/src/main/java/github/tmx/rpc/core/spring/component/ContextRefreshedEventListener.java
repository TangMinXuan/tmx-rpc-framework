package github.tmx.rpc.core.spring.component;

import github.tmx.rpc.core.netty.server.NettyServer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * 如果存在 NettyServer 的实例(即扫描到 @RpcServer ), 就启动服务器
 * @author: TangMinXuan
 * @created: 2020/11/03 19:56
 */
@Component
public class ContextRefreshedEventListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 防止重复执行。
        if (event.getApplicationContext().getParent() ==  null ){
            NettyServer.tryToStartWork();
        }
    }
}
