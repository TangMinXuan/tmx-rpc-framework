package github.tmx.rpc.core.netty.client;

import github.tmx.rpc.core.common.DTO.RpcRequest;
import github.tmx.rpc.core.common.DTO.RpcResponse;
import github.tmx.rpc.core.common.enumeration.RpcResponseEnum;
import github.tmx.rpc.core.config.RpcConfig;
import github.tmx.rpc.core.config.RpcPropertyEnum;
import github.tmx.rpc.core.extension.ExtensionLoader;
import github.tmx.rpc.core.netty.codec.NettyMsgDecoder;
import github.tmx.rpc.core.netty.codec.NettyMsgEncoder;
import github.tmx.rpc.core.registry.ServiceDiscovery;
import github.tmx.rpc.core.spring.BeanNameUtil;
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
    private final ServiceDiscovery serviceDiscovery;

    private final int RETRY_INTERVAL = Integer.valueOf(RpcConfig.getProperty(RpcPropertyEnum.CLIENT_RETRY_INTERVAL));
    private final int RETRY_COUNT = Integer.valueOf(RpcConfig.getProperty(RpcPropertyEnum.CLIENT_RETRY_COUNT));
    private final int PING_INTERVAL = Integer.valueOf(RpcConfig.getProperty(RpcPropertyEnum.CLIENT_PING_INTERVAL));
    private final int MAX_PAUSE_TIME = Integer.valueOf(RpcConfig.getProperty(RpcPropertyEnum.CLIENT_MAX_PAUSE_TIME));

    private Bootstrap bootstrap;

    private NettyClient() {
        channelCacheMap = new ConcurrentHashMap<>();
        serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("Zookeeper");
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
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
                        ch.pipeline().addLast(new IdleStateHandler(MAX_PAUSE_TIME, PING_INTERVAL, 0, TimeUnit.SECONDS));
                        ch.pipeline().addLast(new NettyMsgDecoder());
                        ch.pipeline().addLast(new NettyMsgEncoder());
                        ch.pipeline().addLast(new ClientHeartbeatHandler());
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

        // 向 Zk 寻找 provider 地址
        String serviceName = BeanNameUtil.getBeanName(rpcRequest.getInterfaceName(), rpcRequest.getGroup(), rpcRequest.getVersion());
        InetSocketAddress providerAddress = serviceDiscovery.lookupService(serviceName);
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
                    logger.debug("发送请求成功: {}", rpcRequest);
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
        connectWithRetryPolicy(inetSocketAddress, RETRY_COUNT, completableFuture);
        Channel channel = completableFuture.get();
        channelCacheMap.put(inetSocketAddress, channel);
        return channel;
    }

    private void connectWithRetryPolicy(InetSocketAddress inetSocketAddress, int retryCount,
                                        CompletableFuture<Channel> completableFuture) {
        bootstrap.connect(inetSocketAddress).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess() && future.channel().isActive()) {
                    completableFuture.complete(future.channel());
                    return ;
                } else {
                    bootstrap.config().group().schedule(() ->
                            connectWithRetryPolicy(inetSocketAddress, retryCount - 1,
                                    completableFuture), RETRY_INTERVAL, TimeUnit.SECONDS);
                }
            }
        });
    }

    public void removeChannel(InetSocketAddress inetSocketAddress) {
        channelCacheMap.remove(inetSocketAddress);
    }
}
