package github.tmx.server.netty;

import github.tmx.common.RpcRequest;
import github.tmx.common.RpcResponse;
import github.tmx.server.DefaultServiceRegistry;
import github.tmx.server.RpcRequestHandler;
import github.tmx.server.ServiceRegistry;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: TangMinXuan
 * @created: 2020/10/02 21:13
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private static RpcRequestHandler rpcRequestHandler = new RpcRequestHandler();
    private static ServiceRegistry serviceRegistry = new DefaultServiceRegistry();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            // 这里的 msg 已经被前面的 kryoSerializer 反序列化过了，可以直接转为 RpcRequest
            RpcRequest rpcRequest = (RpcRequest) msg;
            logger.info("服务器收到请求: {}", rpcRequest);

            // 执行具体的接口逻辑
            String requestInterfaceName = rpcRequest.getInterfaceName();
            Object serviceImpl = serviceRegistry.getService(requestInterfaceName);
            Object result = rpcRequestHandler.handle(rpcRequest, serviceImpl);
            logger.info("服务器执行结果: {}", result.toString());

            ChannelFuture channelFuture = ctx.writeAndFlush(RpcResponse.success(result));
            channelFuture.addListener(ChannelFutureListener.CLOSE);     // ?
        } finally {
            ReferenceCountUtil.release(msg);       // 这里是为了避免内存泄漏
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
