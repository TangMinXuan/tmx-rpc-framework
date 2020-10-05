package github.tmx.server.netty;

import github.tmx.common.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author: TangMinXuan
 * @created: 2020/10/02 16:06
 */
public class NettyKryoDecoder extends ByteToMessageDecoder {

    private Serializer serializer;
    private Class<?> targetClass;

    //记录 有效长度 的占位符
    private static final int BODY_LENGTH = 4;

    public NettyKryoDecoder(Serializer serializer, Class<?> targetClass) {
        this.serializer = serializer;
        this.targetClass = targetClass;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        if (byteBuf.readableBytes() >= BODY_LENGTH) {
            byteBuf.markReaderIndex();
            int dataLength = byteBuf.readInt();
            if (dataLength < 0 || byteBuf.readableBytes() < 0) {
                return;
            }
            if (byteBuf.readableBytes() < dataLength) {
                byteBuf.resetReaderIndex();
                return;
            }

            //数据不存在丢失，开始 decode
            byte[] body = new byte[dataLength];
            byteBuf.readBytes(body);
            Object obj = serializer.deserialize(body, targetClass);
            out.add(obj);
        }
    }
}
