package github.tmx.rpc.core.netty.client;

import github.tmx.rpc.core.common.DTO.RpcRequest;
import github.tmx.rpc.core.common.DTO.RpcResponse;
import github.tmx.rpc.core.common.enumeration.RpcMessageTypeEnum;
import github.tmx.rpc.core.config.RpcConfig;
import github.tmx.rpc.core.config.RpcPropertyEnum;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.UUID;

/**
 * @author: TangMinXuan
 * @created: 2020/10/17 11:14
 */
public class ClientHeartbeatHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ClientHeartbeatHandler.class);

    private static Integer HEARTBEAT_THRESHOLD = Integer.valueOf(RpcConfig.getProperty(RpcPropertyEnum.CLIENT_HEARTBEAT_THRESHOLD));

    /**
     * 如果是 RPC 请求的回应消息, 直接放行
     * 如果是 PONG 消息, 则截获
     * @param ctx
     * @param msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        RpcResponse rpcResponse = (RpcResponse) msg;
        if (!rpcResponse.getMessageTypeEnum().equals(RpcMessageTypeEnum.HEARTBEAT_PONG)) {
            logger.info("收到的是 RPC 回应, 直接放行");
            ctx.fireChannelRead(msg);
            return ;
        }
        logger.info("收到一个 PONG 消息");
    }

    /**
     * 一段时间客户端没有发送 RPC 请求, 也即一段时间客户端没有 写事件 发生,
     * 那么我在这个方法中主动发送一个 PING 消息
     * @param ctx
     * @param evt
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                // 触发 写事件 说明, 客户端有一段时间没有发送RPC请求了, 此时发送PING请求维持连接
                logger.info("客户端触发了 写事件 , 即将发送 PING 请求");
                if (isPINGReached(ctx.channel())) {
                    ctx.close();
                }
                RpcRequest rpcRequest = RpcRequest.builder()
                        .requestId(UUID.randomUUID().toString())
                        .messageTypeEnum(RpcMessageTypeEnum.HEARTBEAT_PING)
                        .build();
                ChannelFuture channelFuture = ctx.writeAndFlush(rpcRequest);
                channelFuture.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } else if (state == IdleState.READER_IDLE) {
                // 触发 读事件 说明, 无论是RPC请求, 还是PING请求, 都未能得到服务器的响应, 此时认为服务器宕机
                logger.info("客户端触发了 读事件 , 客户端认为服务端宕机, 主动关闭连接");
                ctx.close();
            }
        } else {
            // 如果不是 IdleStateEvent 就往后传, 不做处理
            ctx.fireUserEventTriggered(evt);
        }
    }

    /**
     * 判断在一个 channel 上发送的 PING 数量是否达到阈值
     * @param channel
     * @return
     */
    private boolean isPINGReached(Channel channel) {
        AttributeKey<Integer> ping_count_key = AttributeKey.valueOf("ping_count");
        if (!channel.hasAttr(ping_count_key) || channel.attr(ping_count_key).get() == null) {
            logger.info("为首次创建的 channel 设置 PING count 数为0");
            channel.attr(ping_count_key).set(0);
            return false;
        }
        Integer count = channel.attr(ping_count_key).get();
        channel.attr(ping_count_key).set(++count);
        logger.info("channel Ping 数为: {}", count);
        if (count >= HEARTBEAT_THRESHOLD) {
            logger.info("达到阈值, 即将断开连接");
            return true;
        }
        return false;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("客户端 channelInactive() 方法被触发");
        NettyClient nettyClient = NettyClient.getInstance();
        nettyClient.removeChannel((InetSocketAddress) ctx.channel().remoteAddress());
    }
}
