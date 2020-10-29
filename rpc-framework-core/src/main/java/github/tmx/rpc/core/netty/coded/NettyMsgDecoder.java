package github.tmx.rpc.core.netty.coded;

import github.tmx.rpc.core.common.DTO.RpcProtocol;
import github.tmx.rpc.core.common.DTO.RpcRequest;
import github.tmx.rpc.core.common.DTO.RpcResponse;
import github.tmx.rpc.core.extension.ExtensionLoader;
import github.tmx.rpc.core.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author: TangMinXuan
 * @created: 2020/10/27 15:40
 */
public class NettyMsgDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(NettyMsgDecoder.class);

    // 最短有效长度
    private static final int MINIMUM_EFFECTIVE_LENGTH = 14;
    private Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension("Kryo");

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        if (byteBuf.readableBytes() < MINIMUM_EFFECTIVE_LENGTH) {
            logger.info("数据不全, 直接返回");
            return ;
        }
        byteBuf.markReaderIndex();

        // 检查魔数, 如果不符合就直接返回
        byte[] magicNum = new byte[4];
        byteBuf.readBytes(magicNum);
        if (!checkMagicNum(magicNum)) {
            logger.error("数据 魔数 错误");
            return ;
        }

        // 检查协议版本
        byte version = byteBuf.readByte();
        if (version != (byte) 1) {
            logger.error("数据 协议版本 错误");
            return ;
        }

        // 检查 length
        int length = byteBuf.readInt();
        if (byteBuf.readableBytes() < length) {
            logger.info("数据不全, 重置读指针后返回");
            byteBuf.resetReaderIndex();
            return ;
        }

        // 序列化工具 codec
        byte codec = byteBuf.readByte();

        // 消息类型: 0-request, 1-response
        byte type = byteBuf.readByte();

        // 反序列 body 的内容
        int bodyLen = length - 2;
        byte[] body = new byte[bodyLen];
        byteBuf.readBytes(body);
        Object obj = null;
        if (type == 0) {
            logger.info("反序列化-->RpcRequest");
            obj = serializer.deserialize(body, RpcRequest.class);
        } else if (type == 1) {
            logger.info("反序列化-->RpcResponse");
            obj = serializer.deserialize(body, RpcResponse.class);
        } else {
            logger.info("未知消息类型");
        }
        out.add(obj);
    }

    private boolean checkMagicNum(byte[] magicNum) {
        for (int i = 0; i < magicNum.length; i++) {
            byte cur = magicNum[i];
            if (cur != RpcProtocol.MAGIC_NUM[i]) {
                return false;
            }
        }
        return true;
    }
}
