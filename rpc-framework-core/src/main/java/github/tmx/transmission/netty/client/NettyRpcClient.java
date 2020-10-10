package github.tmx.transmission.netty.client;

import github.tmx.common.DTO.RpcRequest;
import github.tmx.common.DTO.RpcResponse;
import github.tmx.common.utils.ResponseChecker;
import github.tmx.serialize.kryo.KryoSerializer;
import github.tmx.transmission.RpcClient;
import github.tmx.transmission.netty.coded.NettyKryoDecoder;
import github.tmx.transmission.netty.coded.NettyKryoEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: TangMinXuan
 * @created: 2020/10/05 10:19
 */
public class NettyRpcClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyRpcClient.class);

    private String host;
    private int port;
    private static final Bootstrap bootstrap;

    public NettyRpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    static {
        KryoSerializer kryoSerializer = new KryoSerializer();
        EventLoopGroup clientGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(clientGroup)
                .channel(NioSocketChannel.class)    // channel() 方法指定了 Channel 的实现类
                .option(ChannelOption.SO_KEEPALIVE, true)   // 设置 ChannelOption, 其将被应用到每个新创建的 Channel 的 ChannelConfig
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcResponse.class));
                        ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcRequest.class));
                        ch.pipeline().addLast(new NettyClientHandler());
                    }
                });
    }


    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        try {
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            logger.info("客户端连接: {}", host + ":" + port);
            Channel channel = channelFuture.channel();
            if (channel != null) {
                channel.writeAndFlush(rpcRequest).addListener(future -> {
                    if (future.isSuccess()) {
                        logger.info(String.format("客户端发送消息: %s", rpcRequest.toString()));
                    } else {
                        logger.error("Send failed:", future.cause());
                    }
                });
                channel.closeFuture().sync();
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse" + rpcRequest.getRequestId());
                RpcResponse rpcResponse = channel.attr(key).get();

                //检查 rpcResponse
                ResponseChecker.check(rpcResponse, rpcRequest);

                return rpcResponse.getData();
            }
        } catch (InterruptedException e) {
            logger.error("occur exception when connect server:", e);
        }
        return null;
    }
}
