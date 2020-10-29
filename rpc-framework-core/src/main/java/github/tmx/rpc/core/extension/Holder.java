package github.tmx.rpc.core.extension;

/**
 * @author: TangMinXuan
 * @created: 2020/10/29 10:16
 */
public class Holder<T> {

    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
