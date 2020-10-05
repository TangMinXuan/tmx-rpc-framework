package github.tmx.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class RpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 715745410605631233L;

    private Integer code;
    private String message;
    private T data;

    public static <T> RpcResponse<T> success(T data) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseEnum.SUCCESS.getCode());
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
