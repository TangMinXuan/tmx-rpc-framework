package github.tmx.rpc.core.netty.codec;

import github.tmx.rpc.core.common.DTO.Protocol;
import github.tmx.rpc.core.common.DTO.RpcRequest;
import github.tmx.rpc.core.common.DTO.RpcResponse;
import github.tmx.rpc.core.common.exception.SerializeException;
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

    /**
     * 最短有效长度 = 4(魔数) + 1(协议版本) + 4(后续内容长度int) = 9
     */
    private static final int MINIMUM_EFFECTIVE_LENGTH = 9;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) {
        if (byteBuf.readableBytes() < MINIMUM_EFFECTIVE_LENGTH) {
            logger.debug("数据不全, 直接返回");
            return ;
        }
        byteBuf.markReaderIndex();

        // 检查魔数, 如果不符合就直接返回
        byte[] magicNum = new byte[4];
        byteBuf.readBytes(magicNum);
        if (!checkMagicNum(magicNum)) {
            throw new SerializeException("数据魔数错误");
        }

        // 检查协议版本
        byte version = byteBuf.readByte();
        if (version != (byte) 1) {
            throw new SerializeException("数据协议版本错误");
        }

        // 检查 length
        int length = byteBuf.readInt();
        if (byteBuf.readableBytes() < length) {
            logger.debug("数据不全, 重置读指针后返回");
            byteBuf.resetReaderIndex();
            return ;
        }

        // 序列化工具 codec
        int codecLength = byteBuf.readByte();
        byte[] codec = new byte[codecLength];
        byteBuf.readBytes(codec);
        String codecStr = new String(codec);
        Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(codecStr);


        // 消息类型: 0-request, 1-response
        byte type = byteBuf.readByte();

        // 反序列 body 的内容
        int bodyLen = length - 2 - codecLength;
        byte[] body = new byte[bodyLen];
        byteBuf.readBytes(body);
        Object obj;
        if (type == 0) {
            obj = serializer.deserialize(body, RpcRequest.class);
        } else if (type == 1) {
            obj = serializer.deserialize(body, RpcResponse.class);
        } else {
            throw new SerializeException("未知的 type 类型");
        }
        out.add(obj);
    }

    private boolean checkMagicNum(byte[] magicNum) {
        for (int i = 0; i < magicNum.length; i++) {
            byte cur = magicNum[i];
            if (cur != Protocol.MAGIC_NUM[i]) {
                return false;
            }
        }
        return true;
    }
}
