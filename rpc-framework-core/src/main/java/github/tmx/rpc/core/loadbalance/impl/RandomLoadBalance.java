package github.tmx.rpc.core.loadbalance.impl;

import github.tmx.rpc.core.loadbalance.AbstractLoadBalance;

import java.util.List;
import java.util.Random;

/**
 * @author: TangMinXuan
 * @created: 2020/11/02 11:19
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    @Override
    protected String doSelect(List<String> serviceAddressList, String serviceName) {
        Random random = new Random();
        return serviceAddressList.get(random.nextInt(serviceAddressList.size()));
    }
}
