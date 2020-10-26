package github.tmx.rpc.core.common.DTO;

import github.tmx.rpc.core.common.enumeration.RpcMessageTypeEnum;
import github.tmx.rpc.core.common.enumeration.RpcResponseEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class RpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 715745410605631233L;

    private Integer code;
    private String message;
    private T data;

    private String requestId;

    private RpcMessageTypeEnum messageTypeEnum;

    public static <T> RpcResponse<T> success(T data, String requestId, boolean isHeartBeat) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseEnum.SUCCESS.getCode());
        response.setMessage(RpcResponseEnum.SUCCESS.getMessage());
        response.setRequestId(requestId);
        if (null != data) {
            response.setData(data);
        }
        if (isHeartBeat) {
            response.setMessageTypeEnum(RpcMessageTypeEnum.HEARTBEAT_PONG);
        } else {
            response.setMessageTypeEnum(RpcMessageTypeEnum.RPC_REQUEST);
        }
        return response;
    }

    public static <T> RpcResponse<T> fail(String requestId, RpcResponseEnum RpcConstant) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setCode(RpcConstant.getCode());
        response.setMessage(RpcConstant.getMessage());
        response.setData(null);
        response.setMessageTypeEnum(RpcMessageTypeEnum.RPC_REQUEST);
        return response;
    }

}
