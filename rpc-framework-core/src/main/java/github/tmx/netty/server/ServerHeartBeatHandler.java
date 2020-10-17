package github.tmx.netty.server;

import github.tmx.common.DTO.RpcRequest;
import github.tmx.common.DTO.RpcResponse;
import github.tmx.common.enumeration.RpcMessageTypeEnum;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: TangMinXuan
 * @created: 2020/10/17 10:12
 */
public class ServerHeartBeatHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ServerHeartBeatHandler.class);

    /**
     * 读取从客户端发来的请求,
     * 如果是 心跳 PING 消息, 就返回一个 PONG 消息回去, 并阻断后面的 Handler
     * 如果是 RPC 请求消息, 直接放行给后面的 Handler
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcRequest rpcRequest = (RpcRequest) msg;
        if (!rpcRequest.getMessageTypeEnum().equals(RpcMessageTypeEnum.HEART_BEAT_PING)) {
            logger.info("收到的是 RPC 请求, 直接放行");
            ctx.fireChannelRead(msg);
            return ;
        }
        logger.info("收到一个 PING 消息");
        try {
            RpcResponse rpcResponse = RpcResponse.success(null, rpcRequest.getRequestId(), true);
            ChannelFuture channelFuture = ctx.writeAndFlush(rpcResponse);
            channelFuture.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 当客户端规定时间内未发送消息, 也即服务端的 读事件 规定时间未被触发,
     * 则最前面的 IdleStateHandler 会抛出一个 read idle event 放上流水线中往下传
     * 我在这个 Handler 中截获此 event 并主动关闭连接
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                logger.info("客户端{}规定时间未发送消息, 服务端主动关闭连接", ctx.channel().remoteAddress());
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("服务端 channelInactive() 方法被触发, 客户端正常关闭连接, 服务端随即关闭连接");
        ctx.close();
    }
}
