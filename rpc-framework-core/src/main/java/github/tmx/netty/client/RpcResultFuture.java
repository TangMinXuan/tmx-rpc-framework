package github.tmx.netty.client;

import github.tmx.common.DTO.RpcRequest;
import github.tmx.common.DTO.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: TangMinXuan
 * @created: 2020/10/15 10:02
 */
public class RpcResultFuture {

    private static final Logger logger = LoggerFactory.getLogger(RpcResultFuture.class);

    private static Map<String, CompletableFuture<RpcResponse>> resultFutureMap = new ConcurrentHashMap<>();

    public static void put(RpcRequest request, CompletableFuture<RpcResponse> future) {
        if (resultFutureMap.containsKey(request.getRequestId())) {
            logger.error("已经存在相同 requestId 的请求");
            return ;
        }
        resultFutureMap.put(request.getRequestId(), future);
    }

    public static void remove(RpcRequest request) {
        resultFutureMap.remove(request.getRequestId());
    }

    public static void complete(RpcResponse response) {
        CompletableFuture<RpcResponse> resultFuture = resultFutureMap.get(response.getRequestId());
        if (resultFuture == null) {
            logger.error("收到 response 的 requestId 错误");
        }
        resultFutureMap.remove(response.getRequestId());
        resultFuture.complete(response);
    }
}
