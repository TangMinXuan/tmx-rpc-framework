package github.tmx.rpc.core.netty.codec;

import github.tmx.rpc.core.common.DTO.Protocol;
import github.tmx.rpc.core.common.DTO.RpcRequest;
import github.tmx.rpc.core.common.DTO.RpcResponse;
import github.tmx.rpc.core.config.ConfigurationEnum;
import github.tmx.rpc.core.config.FrameworkConfiguration;
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

    private final String SERIALIZER = FrameworkConfiguration.getProperty(ConfigurationEnum.SERIALIZER);
    private Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(SERIALIZER);

    @Override
    protected void encode(ChannelHandlerContext ctx, Object obj, ByteBuf out) {
        out.writeBytes(Protocol.MAGIC_NUM);
        out.writeByte(Protocol.VERSION);

        // length = 1(codecLength) + 1(type) + body(字节数组) + codec名字(字节数组)
        byte[] body = serializer.serialize(obj);
        int length = 2 + body.length + SERIALIZER.length();
        out.writeInt(length);

        // codec 组件名的长度(占1字节)
        out.writeByte(SERIALIZER.length());
        // codec 组件名
        out.writeBytes(SERIALIZER.getBytes());

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
