package github.tmx.netty.server;

import github.tmx.common.DTO.RpcRequest;
import github.tmx.common.DTO.RpcResponse;
import github.tmx.netty.coded.NettyKryoDecoder;
import github.tmx.netty.coded.NettyKryoEncoder;
import github.tmx.netty.server.provider.DefaultServiceProviderImpl;
import github.tmx.netty.server.provider.ServiceProvider;
import github.tmx.registry.ServiceRegistry;
import github.tmx.registry.ZkServiceRegistry;
import github.tmx.serialize.kryo.KryoSerializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: TangMinXuan
 * @created: 2020/10/01 19:33
 */
public class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    private static ServerBootstrap bootstrap;
    private static KryoSerializer kryoSerializer = new KryoSerializer();
    private static ServiceRegistry serviceRegistry;
    private static ServiceProvider serviceProvider;

    private static String host;
    private static int port;

    static {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                        ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcRequest.class));
                        ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcResponse.class));
                        ch.pipeline().addLast(new ServerHeartBeatHandler());
                        ch.pipeline().addLast(new NettyServerHandler());
                    }
                })
                // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                .childOption(ChannelOption.TCP_NODELAY, true)
                //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                .option(ChannelOption.SO_BACKLOG, 128);

        // 先注销服务, 再关闭线程池, 目的是先处理完正在进行的事件再关闭
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("服务端执行优雅停机");
            List<String> serviceList = serviceProvider.getAllService();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
            for (String service : serviceList) {
                serviceRegistry.cancelService(service, inetSocketAddress);
            }
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }));
    }

    public NettyServer(String host, int port) {
        NettyServer.host = host;
        NettyServer.port = port;
        serviceRegistry = new ZkServiceRegistry();
        serviceProvider = new DefaultServiceProviderImpl();
    }

    /**
     * @param interfaceImpl 接口实现类
     * @param interfaceClass
     * @param <T> 接口类型
     */
    public <T> void startUp(T interfaceImpl, Class<T> interfaceClass) {
        // 发布服务
        publishService(interfaceImpl, interfaceClass);

        // 启动服务器
        try {
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // TODO(tmx): 未来应升级为扫描注解发布
    private <T> void publishService(T interfaceImpl, Class<T> interfaceClass) {
        serviceProvider.addProvider(interfaceImpl);
        serviceRegistry.registerService(interfaceClass.getCanonicalName(), new InetSocketAddress(host, port));
    }
}
