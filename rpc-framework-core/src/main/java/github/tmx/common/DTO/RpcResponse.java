package github.tmx.common.DTO;

import github.tmx.common.enumeration.RpcResponseEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class RpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 715745410605631233L;

    private Integer code;
    private String message;
    private T data;

    private String requestId;

    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseEnum.SUCCESS.getCode());
        response.setMessage(RpcResponseEnum.SUCCESS.getMessage());
        response.setRequestId(requestId);
        if (null != data) {
            response.setData(data);
        }
        return response;
    }

    public static <T> RpcResponse<T> fail(RpcResponseEnum RpcConstant) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcConstant.getCode());
        response.setMessage(RpcConstant.getMessage());
        return response;
    }

}
