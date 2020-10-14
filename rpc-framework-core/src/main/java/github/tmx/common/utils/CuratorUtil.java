package github.tmx.common.utils;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: TangMinXuan
 * @created: 2020/10/13 10:34
 */
public class CuratorUtil {

    private static final Logger logger = LoggerFactory.getLogger(CuratorUtil.class);

    // 连接参数
    private static final int SLEEP_MS_BETWEEN_RETRIES = 100;
    private static final int MAX_RETRIES = 3;
    private static final String CONNECT_STRING = "119.23.235.40:2181";
    private static final int CONNECTION_TIMEOUT_MS = 10 * 1000;
    private static final int SESSION_TIMEOUT_MS = 60 * 1000;
    public static final String ZK_REGISTER_ROOT_PATH = "/tmx-rpc";

    private static final Map<String, List<String>> serviceAddressCacheMap = new ConcurrentHashMap<>();

    public static CuratorFramework getZkClient() {
        // 重试策略，重试3次，并在两次重试之间等待100毫秒，以防出现连接问题。
        RetryPolicy retryPolicy = new RetryNTimes(
                MAX_RETRIES, SLEEP_MS_BETWEEN_RETRIES);
        return CuratorFrameworkFactory.builder()
                //要连接的服务器(可以是服务器列表)
                .connectString(CONNECT_STRING)
                .retryPolicy(retryPolicy)
                //连接超时时间，10秒
                .connectionTimeoutMs(CONNECTION_TIMEOUT_MS)
                //会话超时时间，60秒
                .sessionTimeoutMs(SESSION_TIMEOUT_MS)
                .build();
    }

    /**
     * 创建临时节点
     * 临时节点驻存在 ZooKeeper 中, 当 provider 和 zk 的连接断掉时被删除
     */
    public static void createEphemeralNode(final CuratorFramework zkClient, final String path) {
        try {
            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (Exception e) {
            logger.error("occur exception:", e);
        }
    }

    /**
     * 获取某个节点下的子节点
     * 其中, 节点是接口名, 子节点是 实现类名 + 实现类地址
     * @param zkClient
     * @param serviceName
     * @return
     */
    public static List<String> getChildrenNodes(final CuratorFramework zkClient, final String serviceName) {
        // 先判断地址缓存中有没有需要的服务, 如果有就直接返回
        if (serviceAddressCacheMap.containsKey(serviceName)) {
            return serviceAddressCacheMap.get(serviceName);
        }
        List<String> providerList = new ArrayList<>();
        String servicePath = CuratorUtil.ZK_REGISTER_ROOT_PATH + "/" + serviceName;
        try {
            providerList = zkClient.getChildren().forPath(servicePath);
            serviceAddressCacheMap.put(serviceName, providerList);
            registerWatcher(zkClient, serviceName);
        } catch (Exception e) {
            logger.error("occur exception:", e);
        }
        return providerList;
    }

    /**
     * 注册监听
     * 监听到子节点变化后, 更新 CacheMap
     * @param serviceName 服务名称
     */
    private static void registerWatcher(CuratorFramework zkClient, String serviceName) {
        String servicePath = CuratorUtil.ZK_REGISTER_ROOT_PATH + "/" + serviceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, false);

        // 添加并启动监听器, 当有事件发生时, 将会触发下面的回调函数
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            serviceAddressCacheMap.put(serviceName, serviceAddresses);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        try {
            pathChildrenCache.start();
        } catch (Exception e) {
            logger.error("occur exception:", e);
        }
    }
}
