package github.tmx.rpc.core.common.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: TangMinXuan
 * @created: 2020/10/26 15:58
 */
@AllArgsConstructor
@Getter
public enum RpcPropertyEnum {

    // Zookeeper 地址
    ZK_ADDRESS("rpc.zookeeper.address", "127.0.0.1:2181"),
    // 整个系统的根目录
    ZK_ROOT_PATH("rpc.zookeeper.rootPath", "/tmx-rpc"),
    // 连接重试间隔(单位: 毫秒)
    ZK_RETRY_INTERVAL("rpc.zookeeper.retryInterval", "100"),
    // 连接重试次数
    ZK_RETRY_COUNT("rpc.zookeeper.retryCount", "3"),

    // 服务器监听的端口
    SERVER_PORT("rpc.server.port", "9999"),
    // 服务器最大容忍客户端停顿时间, 若超过这个时间未收到客户端消息, 主动关闭连接 (读事件 单位: 秒)
    SERVER_MAX_PAUSE_TIME("rpc.server.maxPauseTime", "30"),

    // 客户端 PING 的数量达到这个值时主动关闭连接
    CLIENT_HEARTBEAT_THRESHOLD("rpc.client.heartbeatThreshold", "10"),
    // 客户端 PING 的时间间隔 (写事件 单位: 秒)
    CLIENT_PING_INTERVAL("rpc.client.pingInterval", "5"),
    // 客户端最大容忍服务器响应时间, 若超过这个时间未得到服务器响应则认为服务器宕机, 主动关闭连接 (读事件 单位: 秒)
    CLIENT_MAX_PAUSE_TIME("rpc.client.maxPauseTime", "10"),
    // 客户端连接服务器重试间隔 (单位: 秒)
    CLIENT_RETRY_INTERVAL("rpc.client.retryInterval", "3"),
    // 客户端连接服务器重试次数
    CLIENT_RETRY_COUNT("rpc.client.retryCount", "3");

    private String name;
    private String defaultValue;
}
