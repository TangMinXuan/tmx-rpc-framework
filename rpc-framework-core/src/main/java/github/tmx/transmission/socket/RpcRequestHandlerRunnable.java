package github.tmx.transmission.socket;

import github.tmx.common.RpcRequest;
import github.tmx.common.RpcResponse;
import github.tmx.registry.ServiceRegistry;
import github.tmx.transmission.RpcRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 在 run() 方法中匹配请求的服务，并将结果写入 socket
 */
public class RpcRequestHandlerRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RpcRequestHandlerRunnable.class);

    private Socket socket;
    private RpcRequestHandler rpcRequestHandler;
    private ServiceRegistry serviceRegistry;

    public RpcRequestHandlerRunnable(Socket socket, RpcRequestHandler rpcRequestHandler, ServiceRegistry serviceRegistry) {
        this.socket = socket;
        this.rpcRequestHandler = rpcRequestHandler;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void run() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            String interfaceName = rpcRequest.getInterfaceName();
            Object service = serviceRegistry.getService(interfaceName);
            Object result = rpcRequestHandler.handle(rpcRequest, service);
            objectOutputStream.writeObject(RpcResponse.success(result));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("occur exception:", e);
        }
    }
}
