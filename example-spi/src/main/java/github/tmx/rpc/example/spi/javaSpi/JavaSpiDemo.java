package github.tmx.rpc.example.spi.javaSpi;

import java.util.Iterator;
import java.util.ServiceLoader;

public class JavaSpiDemo {

    public static void main(String[] args) {
        ServiceLoader<PhoneService> load = ServiceLoader.load(PhoneService.class);
        Iterator<PhoneService> iterator = load.iterator();
        while (iterator.hasNext()) {
            PhoneService phoneService = iterator.next();
            phoneService.getPhoneName();
        }
    }
}
