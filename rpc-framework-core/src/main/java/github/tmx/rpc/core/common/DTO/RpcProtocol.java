package github.tmx.rpc.core.common.DTO;

import lombok.Getter;
import lombok.Setter;

/**
 * @author: TangMinXuan
 * @created: 2020/10/27 16:50
 */
@Getter
@Setter
public class RpcProtocol {

    public static final byte[] MAGIC_NUM = {(byte) 't', (byte) 'R', (byte) 'P', (byte) 'C'};
    public static final byte VERSION = 1;

    private int length;
    private byte codec;
    private byte type;
    private byte[] body;
}
