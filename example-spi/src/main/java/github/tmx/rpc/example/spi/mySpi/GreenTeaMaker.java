package github.tmx.rpc.example.spi.mySpi;

public class GreenTeaMaker implements TeaMaker {
    @Override
    public void makeTea() {
        System.out.println("制作绿茶");
    }
}
