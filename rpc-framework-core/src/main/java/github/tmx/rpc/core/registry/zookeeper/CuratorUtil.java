package github.tmx.rpc.core.registry.zookeeper;

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
public final class CuratorUtil {

    private static final Logger logger = LoggerFactory.getLogger(CuratorUtil.class);

    // 连接参数
    private static final int SLEEP_MS_BETWEEN_RETRIES = 100;
    private static final int MAX_RETRIES = 3;
    private static final String CONNECT_STRING = "119.23.235.40:2181";
    private static final int CONNECTION_TIMEOUT_MS = 10 * 1000;
    private static final int SESSION_TIMEOUT_MS = 30 * 1000;
    public static final String ZK_REGISTER_ROOT_PATH = "/tmx-rpc";

    private static final Map<String, List<String>> serviceAddressCacheMap = new ConcurrentHashMap<>();

    private static CuratorFramework zkClient = null;

    public static CuratorFramework getZkClient() {
        if (zkClient == null) {
            RetryPolicy retryPolicy = new RetryNTimes(MAX_RETRIES, SLEEP_MS_BETWEEN_RETRIES);
            zkClient = CuratorFrameworkFactory.builder()
                    //要连接的服务器(可以是服务器列表)
                    .connectString(CONNECT_STRING)
                    .retryPolicy(retryPolicy)
                    //连接超时时间，10秒
                    .connectionTimeoutMs(CONNECTION_TIMEOUT_MS)
                    //会话超时时间，30秒
                    .sessionTimeoutMs(SESSION_TIMEOUT_MS)
                    .build();
            zkClient.start();
        }
        return zkClient;
    }

    /**
     * 创建临时节点
     * 临时节点驻存在 ZooKeeper 中, 当 provider 和 zk 的连接断掉时被删除
     */
    public static void createEphemeralNode(CuratorFramework zkClient, String path) {
        try {
            if (zkClient.checkExists().forPath(path) != null) {
                logger.info("节点已经存在, 即将删除");
                zkClient.delete().forPath(path);
            }
            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (Exception e) {
            logger.error("创建临时节点时发生异常: ", e);
        }
    }

    /**
     * 获取某个节点下的子节点
     * 其中, 节点是接口名, 子节点是 实现类名 + 实现类地址
     * @param zkClient
     * @param serviceName
     * @return
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String serviceName) {
        // 先判断地址缓存中有没有需要的服务, 如果有就直接返回
        if (serviceAddressCacheMap.containsKey(serviceName)) {
            return serviceAddressCacheMap.get(serviceName);
        }
        // 缓存中没有需要的地址, 重新去 ZK 中获取
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
     * 客户端注册监听
     * 监听到子节点变化后, 更新 CacheMap
     * @param serviceName 服务名称
     */
    private static void registerWatcher(CuratorFramework zkClient, String serviceName) {
        String servicePath = CuratorUtil.ZK_REGISTER_ROOT_PATH + "/" + serviceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, false);

        // 添加并启动监听器, 当有事件发生时, 将会触发下面的回调函数
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            logger.info("监听到 Zookeeper 节点发生变化, 即将更新 providerList 缓存");
            logger.info("新的 providerList 是: {}", serviceAddresses);
            serviceAddressCacheMap.put(serviceName, serviceAddresses);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        try {
            pathChildrenCache.start();
        } catch (Exception e) {
            logger.error("添加ZK节点监听器时发生异常:", e);
        }
    }

    public static void deleteEphemeralNode(CuratorFramework zkClient, String path) {
        try {
            if (zkClient.checkExists().forPath(path) == null) {
                logger.info("节点不存在, 直接返回");
                return ;
            }
            zkClient.delete().forPath(path);
        } catch (Exception e) {
            logger.error("删除临时节点时发生异常: ", e);
        }
    }
}
