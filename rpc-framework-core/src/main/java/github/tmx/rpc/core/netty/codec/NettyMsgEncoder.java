package github.tmx.rpc.core.netty.codec;

import github.tmx.rpc.core.common.DTO.RpcProtocol;
import github.tmx.rpc.core.common.DTO.RpcRequest;
import github.tmx.rpc.core.common.DTO.RpcResponse;
import github.tmx.rpc.core.extension.ExtensionLoader;
import github.tmx.rpc.core.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author: TangMinXuan
 * @created: 2020/10/27 15:41
 */
public class NettyMsgEncoder extends MessageToByteEncoder {

    private Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension("Kryo");

    @Override
    protected void encode(ChannelHandlerContext ctx, Object obj, ByteBuf out) throws Exception {
        out.writeBytes(RpcProtocol.MAGIC_NUM);
        out.writeByte(RpcProtocol.VERSION);

        // length
        byte[] body = serializer.serialize(obj);
        int length = 2 + body.length;
        out.writeInt(length);

        // codec
        out.writeByte(0);

        // type
        if (obj instanceof RpcRequest) {
            out.writeByte(0);
        } else if (obj instanceof RpcResponse) {
            out.writeByte(1);
        } else {
            out.writeByte(2);
        }

        // body
        out.writeBytes(body);
    }
}
