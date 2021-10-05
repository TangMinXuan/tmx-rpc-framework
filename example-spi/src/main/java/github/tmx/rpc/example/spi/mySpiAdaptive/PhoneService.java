package github.tmx.rpc.example.spi.mySpiAdaptive;

import github.tmx.rpc.core.extension.Adaptive;
import github.tmx.rpc.core.extension.SPI;
import github.tmx.rpc.core.extension.URL;

@SPI
public interface PhoneService {
    @Adaptive
    void getPhoneName(URL url);
}
