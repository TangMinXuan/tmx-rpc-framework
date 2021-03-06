package github.tmx.rpc.core.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import github.tmx.rpc.core.common.DTO.RpcRequest;
import github.tmx.rpc.core.common.DTO.RpcResponse;
import github.tmx.rpc.core.common.exception.SerializeException;
import github.tmx.rpc.core.serialize.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author: TangMinXuan
 * @created: 2020/10/02 10:03
 */
public class KryoSerializer implements Serializer {

    private static final Logger logger = LoggerFactory.getLogger(KryoSerializer.class);

    /**
     * Kryo 不是线程安全的，因此使用 ThreadLocal 为每个线程分配一个 Kryo 对象
     * 一个 ThreadLocal 对象实际上是 threadLocalMap 中的一个 key ，然后每个 Thread 都有自己的 threadLocalMap
     */
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcResponse.class);
        kryo.register(RpcRequest.class);
        kryo.setReferences(true);
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    @Override
    public byte[] serialize(Object object) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = KRYO_THREAD_LOCAL.get();
            kryo.writeObject(output, object);

            // 这里为什么要 remove 掉 kryo ？是每次执行都是一个新线程来执行吗？
            KRYO_THREAD_LOCAL.remove();

            //这里为什么调 toBytes() ？为什么要新建一个字节数组去拷贝 buffer ？也就是为什么不调 getBuffer()
            return output.toBytes();
        } catch (IOException e) {
            logger.error("序列化时发生异常: ", e);
            throw new SerializeException("序列化失败");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = KRYO_THREAD_LOCAL.get();
            KRYO_THREAD_LOCAL.remove();
            return kryo.readObject(input, clazz);
        } catch (IOException e) {
            logger.error("反序列化时发生异常: ", e);
            throw new SerializeException("反序列化失败");
        }
    }
}
