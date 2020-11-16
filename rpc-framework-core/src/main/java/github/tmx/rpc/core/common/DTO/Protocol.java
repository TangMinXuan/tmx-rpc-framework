package github.tmx.rpc.core.common.DTO;

import lombok.Getter;
import lombok.Setter;

/**
 * @author: TangMinXuan
 * @created: 2020/11/16 15:22
 */
@Getter
@Setter
public class Protocol {
    public static final byte[] MAGIC_NUM = {(byte) 't', (byte) 'R', (byte) 'P', (byte) 'C'};
    public static final byte VERSION = 1;

    private int length;

    private byte codecLength;
    private byte[] codec;

    private byte type;
    private byte[] body;
}
