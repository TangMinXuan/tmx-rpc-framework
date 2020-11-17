package github.tmx.rpc.core.serialize.hessian;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import github.tmx.rpc.core.common.exception.SerializeException;
import github.tmx.rpc.core.serialize.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author: TangMinXuan
 * @created: 2020/11/16 15:37
 */
public class HessianSerializer implements Serializer {

    private static final Logger logger = LoggerFactory.getLogger(HessianSerializer.class);

    @Override
    public byte[] serialize(Object data) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Hessian2Output out = new Hessian2Output(bos);
            out.writeObject(data);
            out.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            logger.error("序列化时发生异常: ", e);
            throw new SerializeException("序列化失败");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clz) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            Hessian2Input input = new Hessian2Input(bis);
            return (T) input.readObject(clz);
        } catch (IOException e) {
            logger.error("反序列化时发生异常: ", e);
            throw new SerializeException("反序列化失败");
        }
    }
}
