package github.tmx.rpc.core.loadbalance;

import java.util.List;
import java.util.Random;

/**
 * @author: TangMinXuan
 * @created: 2020/11/02 11:19
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    @Override
    protected String doSelect(List<String> serviceAddressList) {
        Random random = new Random();
        return serviceAddressList.get(random.nextInt(serviceAddressList.size()));
    }
}
