package github.tmx.rpc.core.loadbalance;

import github.tmx.rpc.core.extension.SPI;

import java.util.List;

/**
 * @author: TangMinXuan
 * @created: 2020/11/02 11:16
 */
@SPI
public interface LoadBalance {

    /**
     * 对传入的服务提供者 List 做相应策略的选择
     * @param serviceAddressList
     * @return
     */
    String selectServiceAddress(List<String> serviceAddressList);
}
