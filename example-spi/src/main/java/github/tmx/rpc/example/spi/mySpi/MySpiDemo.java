package github.tmx.rpc.example.spi.mySpi;

import github.tmx.rpc.core.extension.ExtensionLoader;

public class MySpiDemo {
    public static void main(String[] args) {
        TeaMaker teaMaker = ExtensionLoader.getExtensionLoader(TeaMaker.class).getExtension("green");
        teaMaker.makeTea(); // 使用GreenTeaMaker实现类
    }
}
