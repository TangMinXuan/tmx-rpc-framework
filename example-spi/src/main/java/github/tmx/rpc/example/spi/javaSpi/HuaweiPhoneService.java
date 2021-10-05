package github.tmx.rpc.example.spi.javaSpi;

public class HuaweiPhoneService implements PhoneService {
    @Override
    public void getPhoneName() {
        System.out.println("华为手机");
    }
}
