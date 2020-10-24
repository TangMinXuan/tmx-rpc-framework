package github.tmx.rpc.core.common.utils;

import github.tmx.rpc.core.common.DTO.RpcRequest;
import github.tmx.rpc.core.common.DTO.RpcResponse;
import github.tmx.rpc.core.common.enumeration.RpcErrorMessageEnum;
import github.tmx.rpc.core.common.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: TangMinXuan
 * @created: 2020/10/10 19:46
 */
public class ResponseChecker {

    private static final Logger logger = LoggerFactory.getLogger(ResponseChecker.class);
    public static final String INTERFACE_NAME = "interfaceName";

    private ResponseChecker() {
    }

    public static void check(RpcResponse rpcResponse, RpcRequest rpcRequest) {
        if (rpcResponse == null) {
            logger.error("调用服务失败,serviceName:{}", rpcRequest.getInterfaceName());
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }
}
