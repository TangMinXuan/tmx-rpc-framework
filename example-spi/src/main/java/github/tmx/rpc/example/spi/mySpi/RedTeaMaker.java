package github.tmx.rpc.example.spi.mySpi;

public class RedTeaMaker implements TeaMaker {
    @Override
    public void makeTea() {
        System.out.println("制作红茶");
    }
}
