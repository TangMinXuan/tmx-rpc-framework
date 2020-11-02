package github.tmx.rpc.core.loadbalance;

import github.tmx.rpc.core.extension.SPI;

import java.util.List;

/**
 * @author: TangMinXuan
 * @created: 2020/11/02 11:16
 */
@SPI
public interface LoadBalance {

    String selectServiceAddress(List<String> serviceAddresses);
}
