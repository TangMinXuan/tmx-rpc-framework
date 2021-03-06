package github.tmx.rpc.core.netty.client;

import github.tmx.rpc.core.common.DTO.RpcRequest;
import github.tmx.rpc.core.common.DTO.RpcResponse;
import github.tmx.rpc.core.common.enumeration.RpcMessageTypeEnum;
import github.tmx.rpc.core.common.utils.ResponseChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author: TangMinXuan
 * @created: 2020/10/05 11:15
 */
public class NettyRpcClientProxy implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(NettyRpcClientProxy.class);
    private static final Integer INVOKE_TIME = 30000;
    private final RpcClient rpcClient;
    private final String group;
    private final String version;
    private Map<Class, Object> proxyObjectCacheMap = new HashMap<>();

    public NettyRpcClientProxy(String group, String version) {
        // 多个 nettyRpcClientProxy 共用一个 nettyClient 对象
        rpcClient = NettyClient.getInstance();
        this.group = group;
        this.version = version;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxyInstance(Class<T> clazz) {
        // 缓存 proxy 对象避免频繁的反射导致 OOM
        if (proxyObjectCacheMap.containsKey(clazz)) {
            return (T) proxyObjectCacheMap.get(clazz);
        }
        T proxyObject = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
        proxyObjectCacheMap.put(clazz, proxyObject);
        return proxyObject;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        logger.debug("调用代理对象方法: {}", method.getName());
        RpcRequest rpcRequest = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .messageTypeEnum(RpcMessageTypeEnum.RPC_REQUEST)
                .group(group)
                .version(version)
                .build();
        CompletableFuture<RpcResponse> resultFuture = rpcClient.sendRpcRequest(rpcRequest);
        // 阻塞获取 rpcResponse
        RpcResponse rpcResponse = null;
        try {
            rpcResponse = resultFuture.get(INVOKE_TIME, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            logger.warn("调用时间超时");
            // 超时的情况下, 必须手动 remove 掉 key, 否则会造成泄漏
            RpcResultFuture.remove(rpcRequest);
        }

        ResponseChecker.check(rpcResponse, rpcRequest);
        return Objects.requireNonNull(rpcResponse).getData();
    }
}
