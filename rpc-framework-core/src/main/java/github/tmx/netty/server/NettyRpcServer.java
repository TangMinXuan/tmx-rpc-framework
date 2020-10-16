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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @author: TangMinXuan
 * @created: 2020/10/01 19:33
 */
public class NettyRpcServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyRpcServer.class);

    private final String host;
    private final int port;
    private final KryoSerializer kryoSerializer;
    private final ServiceRegistry serviceRegistry;
    private final ServiceProvider serviceProvider;

    public NettyRpcServer(String host, int port) {
        this.host = host;
        this.port = port;
        kryoSerializer = new KryoSerializer();
        serviceRegistry = new ZkServiceRegistry();
        serviceProvider = new DefaultServiceProviderImpl();
    }

    /**
     * connect() 和 bind() 的异同?
     * 二者都由 BootStrap 调用, 并返回一个 ChannelFuture 对象
     * 在客户端常用 connect() 方法, 表示, 主动的连接至远程的一个节点
     * 在服务端常用 bind() 方法, 表示, 被动的在某一个端口等待连接
     *
     * 关于服务器引导类中的【父Channel】和【子Channel】的区别?
     * 1) 每当 ServerBootstrap 调用 bind() 方法绑定一个端口时，将会创建一个 ServerChannel, 这个是 【父Channel】
     * 2) 每当一个客户端连接进来时，会创建新的 Channel, 这个是 【子Channel】
     * ServerBootstrap 中的 option() 和 childOption() 就是区分到底为那个 Channel 配置东西
     * 具体有哪些配置参考 ChannelConfig 的 API 文档
     * @param interfaceImpl 接口实现类
     * @param interfaceClass
     * @param <T> 接口类型
     */
    public <T> void startUp(T interfaceImpl, Class<T> interfaceClass) {
        // 发布服务
        publishService(interfaceImpl, interfaceClass);

        // 启动网络通信
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcRequest.class));
                            ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcResponse.class));
                            ch.pipeline().addLast(new NettyServerHandler());
                        }
                    })
                    // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 是否开启 TCP 底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 128);
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("启动服务器时发生异常:", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    // TODO(tmx): 未来应升级为扫描注解发布
    private <T> void publishService(T interfaceImpl, Class<T> interfaceClass) {
        serviceProvider.addProvider(interfaceImpl);
        serviceRegistry.registerService(interfaceClass.getCanonicalName(), new InetSocketAddress(host, port));
    }
}
