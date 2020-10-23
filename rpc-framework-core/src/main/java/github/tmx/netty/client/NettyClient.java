package github.tmx.netty.client;

import github.tmx.common.DTO.RpcRequest;
import github.tmx.common.DTO.RpcResponse;
import github.tmx.common.enumeration.RpcResponseEnum;
import github.tmx.netty.coded.NettyKryoDecoder;
import github.tmx.netty.coded.NettyKryoEncoder;
import github.tmx.registry.zookeeper.ZkServiceDiscovery;
import github.tmx.serialize.kryo.KryoSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author: TangMinXuan
 * @created: 2020/10/05 10:19
 */
public class NettyClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private volatile static NettyClient nettyClient = null;

    private final Map<InetSocketAddress, Channel> channelCacheMap;
    private final ZkServiceDiscovery zkServiceDiscovery;
    private final int delay = 3;
    private Bootstrap bootstrap;

    private NettyClient() {
        channelCacheMap = new ConcurrentHashMap<>();
        zkServiceDiscovery = new ZkServiceDiscovery();
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        KryoSerializer kryoSerializer = new KryoSerializer();
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
                        ch.pipeline().addLast(new IdleStateHandler(10, 5, 0, TimeUnit.SECONDS));
                        ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcResponse.class));
                        ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcRequest.class));
                        ch.pipeline().addLast(new ClientHeartBeatHandler());
                        ch.pipeline().addLast(new NettyClientHandler());
                    }
                });
        // 注册一个 关闭钩子 用于 优雅停机
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("客户端执行优雅停机");
            eventLoopGroup.shutdownGracefully();
        }));
    }

    public static NettyClient getInstance() {
        if (nettyClient == null) {
            synchronized (NettyClient.class) {
                if (nettyClient == null) {
                    nettyClient = new NettyClient();
                }
            }
        }
        return nettyClient;
    }

    public Bootstrap getClientBootstrap() {
        return bootstrap;
    }


    @SneakyThrows
    @Override
    public CompletableFuture<RpcResponse> sendRpcRequest(RpcRequest rpcRequest) {
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();

        // 拿着接口名, 向 Zk 寻找 provider 地址
        InetSocketAddress providerAddress = zkServiceDiscovery.lookupService(rpcRequest.getInterfaceName());
        if (providerAddress == null) {
            resultFuture.complete(RpcResponse.fail(rpcRequest.getRequestId(), RpcResponseEnum.NOT_FOUND_SERVER));
            return resultFuture;
        }

        // 带有重试机制的去连接 provider
        Channel channel = getChannel(providerAddress);

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

    public Channel getChannel(InetSocketAddress inetSocketAddress) throws ExecutionException, InterruptedException {
        if (channelCacheMap.containsKey(inetSocketAddress)) {
            Channel channel = channelCacheMap.get(inetSocketAddress);
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channelCacheMap.remove(inetSocketAddress);
            }
        }
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        connectWithRetryPolicy(inetSocketAddress, 3, completableFuture);
        Channel channel = completableFuture.get();
        channelCacheMap.put(inetSocketAddress, channel);
        return channel;
    }

    private void connectWithRetryPolicy(InetSocketAddress inetSocketAddress, int retryTimes,
                                        CompletableFuture<Channel> completableFuture) {
        bootstrap.connect(inetSocketAddress).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess() && future.channel().isActive()) {
                    completableFuture.complete(future.channel());
                    return ;
                } else {
                    bootstrap.config().group().schedule(() ->
                            connectWithRetryPolicy(inetSocketAddress, retryTimes - 1,
                                    completableFuture), delay, TimeUnit.SECONDS);
                }
            }
        });
    }

    public void removeChannel(InetSocketAddress inetSocketAddress) {
        channelCacheMap.remove(inetSocketAddress);
    }
}
