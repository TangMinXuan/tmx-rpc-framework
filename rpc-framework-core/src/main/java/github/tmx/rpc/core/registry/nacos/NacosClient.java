package github.tmx.rpc.core.registry.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import github.tmx.rpc.core.config.ConfigurationEnum;
import github.tmx.rpc.core.config.FrameworkConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NacosClient {

    private static final Logger logger = LoggerFactory.getLogger(NacosClient.class);

    private static String serverAddr = FrameworkConfiguration.getProperty(ConfigurationEnum.NACOS_ADDRESS);

    private static NamingService nacosService = null;

    /**
     * 连接参数
     */
    private static int CONNECTION_TIMEOUT_MS = 10 * 1000;
    private static int SESSION_TIMEOUT_MS = 30 * 1000;
    public static String ROOT_PATH = FrameworkConfiguration.getProperty(ConfigurationEnum.ZK_ROOT_PATH);

    public static NamingService getNacosService() {
        if (nacosService != null) {
            return nacosService;
        }
        try {
            nacosService = NacosFactory.createNamingService(serverAddr);
            return nacosService;
        } catch (NacosException e) {
            logger.error("初始化nacos错误", e);
        }
        return null;
    }
}
