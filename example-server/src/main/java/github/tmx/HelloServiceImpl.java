package github.tmx;

public class HelloServiceImpl implements HelloService {

    @Override
    public Hello sayHello(Hello hello) {
        System.out.println("hello from client: " +
                "id: " + hello.getId() + " " +
                "message: " + hello.getMessage());
        Hello hello_return = new Hello(2, "Hello, client");
        return hello_return;
    }
}
