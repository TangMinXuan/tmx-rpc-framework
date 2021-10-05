package github.tmx.rpc.example.spi.javaSpi;

public class XiaomiPhoneService implements PhoneService {

    @Override
    public void getPhoneName() {
        System.out.println("小米手机");
    }
}
