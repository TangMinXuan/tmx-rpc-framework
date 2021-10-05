package github.tmx.rpc.example.spi.mySpiAdaptive;

import github.tmx.rpc.core.extension.URL;

import java.util.Map;

public class HuaweiPhoneService implements PhoneService {
    @Override
    public void getPhoneName(URL url) {
        System.out.println("华为手机");
        // 获取url传入的参数, 遍历他
        Map<String, String> parameters = url.getParameter();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
    }
}
