package github.tmx.netty.client;

import github.tmx.common.DTO.RpcRequest;
import github.tmx.common.DTO.RpcResponse;

import java.util.concurrent.CompletableFuture;

/**
 * @author: TangMinXuan
 * @created: 2020/10/05 10:21
 */
public interface RpcClient {

    CompletableFuture<RpcResponse> sendRpcRequest(RpcRequest rpcRequest);
}
