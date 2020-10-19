package github.tmx.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
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
 * @created: 2020/10/12 10:52
 */
public class ChannelProvider {
    private static final Logger logger = LoggerFactory.getLogger(ChannelProvider.class);

    private static Map<InetSocketAddress, Channel> channelCacheMap = new ConcurrentHashMap<>();
    private static CompletableFuture<Channel> completableFuture = null;

    private static final int delay = 3;

    private ChannelProvider() {

    }

    public static Channel getChannel(InetSocketAddress inetSocketAddress) throws ExecutionException, InterruptedException {
        if (channelCacheMap.containsKey(inetSocketAddress)) {
            Channel channel = channelCacheMap.get(inetSocketAddress);
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channelCacheMap.remove(inetSocketAddress);
            }
        }
        completableFuture = new CompletableFuture<>();
        connectWithRetryPolicy(NettyClient.getBootstrap(), inetSocketAddress, 3);
        Channel channel = completableFuture.get();
        channelCacheMap.put(inetSocketAddress, channel);
        return channel;
    }

    private static void connectWithRetryPolicy(Bootstrap bootstrap, InetSocketAddress inetSocketAddress, int retryTimes) {
        bootstrap.connect(inetSocketAddress).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess() && future.channel().isActive()) {
                    completableFuture.complete(future.channel());
                    return ;
                } else {
                    bootstrap.config().group().schedule(() ->
                            connectWithRetryPolicy(bootstrap, inetSocketAddress, retryTimes - 1), delay, TimeUnit.SECONDS);
                }
            }
        });
    }

    public static void removeChannel(InetSocketAddress inetSocketAddress) {
        channelCacheMap.remove(inetSocketAddress);
    }
}
