package github.tmx.transmission.netty.server;

import github.tmx.common.RpcRequest;
import github.tmx.common.RpcResponse;
import github.tmx.serialize.kryo.KryoSerializer;
import github.tmx.transmission.netty.coded.NettyKryoDecoder;
import github.tmx.transmission.netty.coded.NettyKryoEncoder;
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

/**
 * @author: TangMinXuan
 * @created: 2020/10/01 19:33
 */
public class NettyRpcServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyRpcServer.class);

    private int port;
    private KryoSerializer kryoSerializer;

    public NettyRpcServer(int port) {
        this.port = port;
        kryoSerializer = new KryoSerializer();
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
     *
     *
     */
    public void startUp() {
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
                    .childOption(ChannelOption.TCP_NODELAY, true)   // 设置tcp缓冲区
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("启动服务器时发生异常:", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
