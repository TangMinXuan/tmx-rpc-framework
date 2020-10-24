package github.tmx.rpc.core.common.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseEnum {

    SUCCESS(200, "调用方法成功"),
    FAIL(500, "调用方法失败"),
    NOT_FOUND_METHOD(503, "未找到指定方法"),
    NOT_FOUND_CLASS(502, "未找到指定类"),
    NOT_FOUND_SERVER(501, "未找到服务提供者");

    private final int code;

    private final String message;
}
