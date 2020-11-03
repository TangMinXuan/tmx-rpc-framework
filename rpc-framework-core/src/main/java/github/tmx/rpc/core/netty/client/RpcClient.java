package github.tmx.rpc.core.netty.client;

import github.tmx.rpc.core.common.DTO.RpcRequest;
import github.tmx.rpc.core.common.DTO.RpcResponse;

import java.util.concurrent.CompletableFuture;

/**
 * @author: TangMinXuan
 * @created: 2020/10/05 10:21
 */
public interface RpcClient {

    /**
     * 发送 Rpc 请求
     * @param rpcRequest
     * @return
     */
    CompletableFuture<RpcResponse> sendRpcRequest(RpcRequest rpcRequest);
}
