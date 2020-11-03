package github.tmx.rpc.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author: TangMinXuan
 * @created: 2020/10/24 20:46
 */
public final class RpcConfig {

    private static final Logger logger = LoggerFactory.getLogger(RpcConfig.class);

    private static final String PROPERTIES_FILE_NAME = "rpc.properties";

    private static Properties userProperties;

    static {
        // 下面的语句是, 获取调用者的根目录
        // 获取 core 目录下此类所在的路径: RpcConfig.class.getResource("").getPath()
        // 在 idea 中, 被标记为 resources 的文件夹在生成 target 时会被抹去
        // 因此 resources 中的文件都视为在根目录
        String userPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        userProperties = readPropertiesFile(userPath);
    }

    private RpcConfig() {

    }

    private static Properties readPropertiesFile(String path) {
        File propertiesFile = new File(path + PROPERTIES_FILE_NAME);
        Properties properties = new Properties();
        if (!propertiesFile.exists()) {
            return properties;
        }
        try (FileInputStream fileInputStream = new FileInputStream(propertiesFile)) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            logger.error("读取 .properties 文件时发生 IO 错误: ", e);
        }
        return properties;
    }

    public static String getProperty(RpcPropertyEnum property) {
        return userProperties.getProperty(property.getName(), property.getDefaultValue());
    }
}
