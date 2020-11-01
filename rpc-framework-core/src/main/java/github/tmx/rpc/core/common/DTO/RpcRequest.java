package github.tmx.rpc.core.common.DTO;

import github.tmx.rpc.core.common.enumeration.RpcMessageTypeEnum;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;

    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
    private RpcMessageTypeEnum messageTypeEnum;
    private String version;
    private String group;

    public String getServiceName() {
        return interfaceName + "[" + group + "]" + "[" + version + "]";
    }

}
