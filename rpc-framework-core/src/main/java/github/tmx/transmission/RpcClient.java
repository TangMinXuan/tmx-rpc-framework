package github.tmx.transmission;

import github.tmx.common.RpcRequest;

/**
 * @author: TangMinXuan
 * @created: 2020/10/05 10:21
 */
public interface RpcClient {

    Object sendRpcRequest(RpcRequest rpcRequest);
}
