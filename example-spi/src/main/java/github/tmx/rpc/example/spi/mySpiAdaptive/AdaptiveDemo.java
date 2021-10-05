package github.tmx.rpc.example.spi.mySpiAdaptive;

import github.tmx.rpc.core.extension.ExtensionLoader;
import github.tmx.rpc.core.extension.URL;

public class AdaptiveDemo {
    public static void main(String[] args) {
        URL url_1 = URL.valueOf("dubbo://127.0.0.1:20880?phone.service=xiaomi&key1=value1");
        URL url_2 = URL.valueOf("dubbo://127.0.0.1:20880?phone.service=huawei&hello=huawei");
        PhoneService phoneService = ExtensionLoader.getExtensionLoader(PhoneService.class).getAdaptiveExtension();
        phoneService.getPhoneName(url_1);
        System.out.println("——————————————————————————");
        phoneService.getPhoneName(url_2);
    }
}
