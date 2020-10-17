package github.tmx.netty.client;

import github.tmx.common.DTO.RpcRequest;
import github.tmx.common.DTO.RpcResponse;
import github.tmx.common.enumeration.RpcMessageTypeEnum;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * @author: TangMinXuan
 * @created: 2020/10/17 11:14
 */
public class ClientHeartBeatHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ClientHeartBeatHandler.class);

    /**
     * 如果是 RPC 请求的回应消息, 直接放行
     * 如果是 PONG 消息, 则截获
     * @param ctx
     * @param msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        RpcResponse rpcResponse = (RpcResponse) msg;
        if (!rpcResponse.getMessageTypeEnum().equals(RpcMessageTypeEnum.HEART_BEAT_PONG)) {
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
                logger.info("客户端触发了 写事件 , 即将发送 PING 请求");
                RpcRequest rpcRequest = RpcRequest.builder()
                        .requestId(UUID.randomUUID().toString())
                        .messageTypeEnum(RpcMessageTypeEnum.HEART_BEAT_PING)
                        .build();
                ChannelFuture channelFuture = ctx.writeAndFlush(rpcRequest);
                channelFuture.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("客户端 channelInactive() 方法被触发");
        ctx.close();
    }
}
