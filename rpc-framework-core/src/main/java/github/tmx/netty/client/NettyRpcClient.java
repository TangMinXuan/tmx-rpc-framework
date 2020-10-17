package github.tmx.netty.client;

import github.tmx.common.DTO.RpcRequest;
import github.tmx.common.DTO.RpcResponse;
import github.tmx.netty.coded.NettyKryoDecoder;
import github.tmx.netty.coded.NettyKryoEncoder;
import github.tmx.registry.ServiceRegistry;
import github.tmx.registry.ZkServiceRegistry;
import github.tmx.serialize.kryo.KryoSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author: TangMinXuan
 * @created: 2020/10/05 10:19
 */
public class NettyRpcClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyRpcClient.class);

    private static final EventLoopGroup eventLoopGroup;
    private static final Bootstrap bootstrap;

    private static ServiceRegistry serviceRegistry;

    static {
        KryoSerializer kryoSerializer = new KryoSerializer();

        // 初始化这个实例时会自动连接远程 Zookeeper
        serviceRegistry = new ZkServiceRegistry();

        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                // channel() 方法指定了 Channel 的实现类
                .channel(NioSocketChannel.class)

                // 设置 ChannelOption, 其将被应用到每个新创建的 Channel 的 ChannelConfig
                // 连接的超时时间，超过这个时间还是建立不上的话则代表连接失败
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcResponse.class));
                        ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcRequest.class));
                        ch.pipeline().addLast(new ClientHeartBeatHandler());
                        ch.pipeline().addLast(new NettyClientHandler());
                    }
                });
    }

    public static Bootstrap getBootstrap() {
        return bootstrap;
    }

    public static void close() {
        logger.info("主动调用close()方法, 客户端关闭");
        eventLoopGroup.shutdownGracefully();
    }

    @Override
    public CompletableFuture<RpcResponse> sendRpcRequest(RpcRequest rpcRequest) {
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();

        // 拿着接口名, 向 Zk 寻找 provider 地址
        InetSocketAddress providerAddress = serviceRegistry.lookupService(rpcRequest.getInterfaceName());

        // 带有重试机制的去连接 provider
        Channel channel = ChannelProvider.getChannel(providerAddress);

        RpcResultFuture.put(rpcRequest, resultFuture);
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    logger.info("发送请求成功: {}", rpcRequest);
                } else {
                    logger.error("发送请求失败: {}, 失败原因: {}", rpcRequest, future.cause());
                }
            });
        } else {
            logger.error("发送请求失败: {}, 失败原因: channel为空或者已经失效", rpcRequest);
        }

        return resultFuture;
    }
}
