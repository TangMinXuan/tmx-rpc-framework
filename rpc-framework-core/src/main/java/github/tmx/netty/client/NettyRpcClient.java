package github.tmx.netty.client;

import github.tmx.common.DTO.RpcRequest;
import github.tmx.common.DTO.RpcResponse;
import github.tmx.common.utils.ResponseChecker;
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
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

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
        serviceRegistry = new ZkServiceRegistry();

        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                // channel() 方法指定了 Channel 的实现类
                .channel(NioSocketChannel.class)

                // 设置 ChannelOption, 其将被应用到每个新创建的 Channel 的 ChannelConfig
                // 连接的超时时间，超过这个时间还是建立不上的话则代表连接失败
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                // 是否开启 TCP 底层心跳机制
                .option(ChannelOption.SO_KEEPALIVE, true)
                // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcResponse.class));
                        ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcRequest.class));
                        ch.pipeline().addLast(new NettyClientHandler());
                    }
                });
    }

    public static Bootstrap getBootstrap() {
        return bootstrap;
    }

    public static void close() {
        logger.info("客户端关闭");
        eventLoopGroup.shutdownGracefully();
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        try {
            // 拿着接口名, 向 Zk 寻找 provider 地址
            InetSocketAddress providerAddress = serviceRegistry.lookupService(rpcRequest.getInterfaceName());

            // 带有重试机制的去连接 provider
            Channel channel = ChannelProvider.get(providerAddress);

            if (channel.isActive()) {
                channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        logger.info("客户端发送请求: {}", rpcRequest);
                    } else {
                        future.channel().close();
                        logger.error("Send failed:", future.cause());
                    }
                });
                channel.closeFuture().sync();
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse" + rpcRequest.getRequestId());
                RpcResponse rpcResponse = channel.attr(key).get();
                logger.info("客户端收到回应:{}", rpcResponse);
                // 校验 RpcResponse 和 RpcRequest
                ResponseChecker.check(rpcResponse, rpcRequest);
                return rpcResponse.getData();
            } else {
                close();
                System.exit(0);
            }

        } catch (InterruptedException e) {
            logger.error("客户端发送RPC请求时发生异常:", e);
        }

        return null;
    }
}
