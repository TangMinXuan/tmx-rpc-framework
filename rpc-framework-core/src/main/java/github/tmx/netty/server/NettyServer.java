package github.tmx.netty.server;

import github.tmx.common.DTO.RpcRequest;
import github.tmx.common.DTO.RpcResponse;
import github.tmx.netty.coded.NettyKryoDecoder;
import github.tmx.netty.coded.NettyKryoEncoder;
import github.tmx.netty.server.provider.DefaultServiceProviderImpl;
import github.tmx.netty.server.provider.ServiceProvider;
import github.tmx.registry.ServiceRegistry;
import github.tmx.registry.zookeeper.ZkServiceRegistry;
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
import java.util.concurrent.TimeUnit;

/**
 * @author: TangMinXuan
 * @created: 2020/10/01 19:33
 */
public class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private volatile static NettyServer nettyServer = null;

    private ServerBootstrap bootstrap;
    private final ServiceRegistry serviceRegistry;
    private final ServiceProvider serviceProvider;
    private final InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 9999);

    private NettyServer() {
        serviceRegistry = new ZkServiceRegistry();
        serviceProvider = new DefaultServiceProviderImpl();
        KryoSerializer kryoSerializer = new KryoSerializer();

        EventLoopGroup workerGroup = new NioEventLoopGroup();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                .childOption(ChannelOption.TCP_NODELAY, true)
                //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                .option(ChannelOption.SO_BACKLOG, 128)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                        ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcRequest.class));
                        ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcResponse.class));
                        ch.pipeline().addLast(new ServerHeartBeatHandler());
                        ch.pipeline().addLast(new NettyServerHandler());
                    }
                });

        // 先注销服务, 再关闭线程池, 目的是先处理完正在进行的事件再关闭
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("服务端执行优雅停机");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }));
    }

    public static NettyServer getInstance() {
        if (nettyServer == null) {
            synchronized (NettyServer.class) {
                if (nettyServer == null) {
                    nettyServer = new NettyServer();
                }
            }
        }
        return nettyServer;
    }

    public void startUp() {
        // 启动服务器
        try {
            ChannelFuture channelFuture = bootstrap.bind(inetSocketAddress.getPort()).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发布服务 = 添加实现类(provider) + 注册服务(registry)
     * @param service 服务接口实现类
     */
    public void publishService(Object service) {
        Class[] interfaces = service.getClass().getInterfaces();
        if (interfaces.length == 0) {
            logger.error("服务没有实现任何接口");
        }
        for (Class i : interfaces) {
            serviceProvider.addProvider(i.getCanonicalName(), service);
            serviceRegistry.registerService(i.getCanonicalName(), inetSocketAddress);
        }
    }
}
