package github.tmx.transmission.netty.coded;

import github.tmx.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author: TangMinXuan
 * @created: 2020/10/02 15:53
 */
public class NettyKryoEncoder extends MessageToByteEncoder {

    private Serializer serializer;
    private Class<?> targetClass;

    public NettyKryoEncoder(Serializer serializer, Class<?> targetClass) {
        this.serializer = serializer;
        this.targetClass = targetClass;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object obj, ByteBuf byteBuf) throws Exception {
        if (targetClass.isInstance(obj)) {
            byte[] bytes = serializer.serialize(obj);
            int length = bytes.length;

            //将数组长度写入 byteBuf ，在 decode 的时候拿出来校验
            //byteBuf在网络传输的过程中是可能发生丢失的？
            byteBuf.writeInt(length);
            byteBuf.writeBytes(bytes);
        }
    }
}
