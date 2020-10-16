package github.tmx.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: TangMinXuan
 * @created: 2020/10/12 10:52
 */
public class ChannelProvider {
    private static final Logger logger = LoggerFactory.getLogger(ChannelProvider.class);

    private static Map<InetSocketAddress, Channel> channelCacheMap = new ConcurrentHashMap<>();

    // 最多重试次数
    private static final int MAX_RETRY_COUNT = 3;

    public static Channel getChannel(InetSocketAddress inetSocketAddress) {
        if (channelCacheMap.containsKey(inetSocketAddress)) {
            Channel channel = channelCacheMap.get(inetSocketAddress);
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channelCacheMap.remove(inetSocketAddress);
            }
        }
        Channel channel = null;
        try {
            channel = connectWithRetryPolicy(inetSocketAddress, MAX_RETRY_COUNT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        channelCacheMap.put(inetSocketAddress, channel);
        return channel;
    }

    private static Channel connectWithRetryPolicy(InetSocketAddress inetSocketAddress, int retryTimes) throws InterruptedException {
        Bootstrap bootstrap = NettyRpcClient.getBootstrap();
        for (int i = 0; i < retryTimes; i++) {
            logger.info("第{}次连接, 此次延迟{}秒", i + 1, i);
            Thread.sleep(i * 1000);
            ChannelFuture future = bootstrap.connect(inetSocketAddress).sync();
            if (future.isSuccess()) {
                return future.channel();
            }
        }
        logger.error("连接远程服务器失败 {}", inetSocketAddress);
        return null;
    }
}
