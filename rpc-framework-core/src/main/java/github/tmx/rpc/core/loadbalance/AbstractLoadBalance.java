package github.tmx.rpc.core.loadbalance;

import java.util.List;

/**
 * @author: TangMinXuan
 * @created: 2020/11/02 11:18
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    @Override
    public String selectServiceAddress(List<String> serviceAddresses) {
        if (serviceAddresses == null || serviceAddresses.size() == 0) {
            return null;
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        return doSelect(serviceAddresses);
    }

    protected abstract String doSelect(List<String> serviceAddresses);
}
