package github.tmx.server.serialize;

/**
 * @author: TangMinXuan
 * @created: 2020/10/02 10:23
 */
public interface Serializer {

    /**
     * 序列化
     * @param object 要序列化的对象
     * @return 字节数组
     */
    byte[] serialize(Object object);

    /**
     * 反序列化
     * @param bytes 字节数组
     * @param clazz 反序列化后的类
     * @param <T>
     * @return 反序列化后的对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
