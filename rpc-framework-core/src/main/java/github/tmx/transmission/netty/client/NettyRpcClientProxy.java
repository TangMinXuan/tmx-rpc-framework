package github.tmx.transmission.netty.client;

import github.tmx.common.RpcRequest;
import github.tmx.transmission.RpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author: TangMinXuan
 * @created: 2020/10/05 11:15
 */
public class NettyRpcClientProxy implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(NettyRpcClientProxy.class);

    RpcClient rpcClient;

    public NettyRpcClientProxy(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    public <T> T getProxyInstance(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        logger.info("调用代理对象方法: {}", method.getName());
        RpcRequest rpcRequest = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .build();
        return rpcClient.sendRpcRequest(rpcRequest);
    }
}
