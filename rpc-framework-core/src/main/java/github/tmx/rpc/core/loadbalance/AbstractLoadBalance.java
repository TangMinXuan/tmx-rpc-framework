package github.tmx.rpc.core.loadbalance;

import java.util.List;

/**
 * @author: TangMinXuan
 * @created: 2020/11/02 11:18
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    @Override
    public String selectServiceAddress(List<String> serviceAddressList) {
        if (serviceAddressList == null || serviceAddressList.size() == 0) {
            return null;
        }
        if (serviceAddressList.size() == 1) {
            return serviceAddressList.get(0);
        }
        return doSelect(serviceAddressList);
    }

    /**
     * 当服务提供者的数目大于1个时, 执行此方法
     * @param serviceAddressList
     * @return
     */
    protected abstract String doSelect(List<String> serviceAddressList);
}
