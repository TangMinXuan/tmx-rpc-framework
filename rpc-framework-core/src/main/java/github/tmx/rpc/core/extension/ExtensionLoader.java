package github.tmx.rpc.core.extension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * SPI 作用: 在运行时，通过修改配置文件, 动态为接口替换实现类
 * file 名: 接口全限定名
 * file 中的 key: 实现类简称
 * file 中的 value: 实现类全限定名
 *
 * @author: TangMinXuan
 * @created: 2020/10/29 10:15
 * @reference: https://dubbo.apache.org/zh-cn/docs/source_code_guide/dubbo-spi.html
 */
public final class ExtensionLoader<T> {

    private static final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);

    private static final String SERVICE_DIRECTORY = "META-INF/extensions/";

    // key: @SPI接口的 class 对象
    // value: ExtensionLoader对象
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();

    // key: 实现类的 class 对象
    // value: 实现类实例对象
    private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    // 接口类 class 对象
    private final Class<?> type;

    // key: 实现类简称
    // value: 实现类实例对象
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    // key: 实现类简称(字符串)
    // value: 实现类的 class 对象
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    private ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    /**
     *
     * @param type 接口 class 对象
     * @param <S>
     * @return
     */
    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type) {
        if (type == null) {
            throw new IllegalArgumentException("扩展接口类不应该为空.");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("扩展接口类应该是一个接口.");
        }
        if (type.getAnnotation(SPI.class) == null) {
            logger.error("扩展接口类: {} 应该被 @SPI 标注", type);
            throw new IllegalArgumentException("扩展接口类应该被 @SPI 标注");
        }

        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        if (extensionLoader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<S>(type));
            extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }

    /**
     *
     * @param name 实现类简称(key名, 例如: Zookeeper)
     * @return
     */
    public T getExtension(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("扩展实现类的名字不应为空.");
        }
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        // instance 是实现类
        Object instance = holder.get();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    /**
     *
     * @param name 实现类简称(key名, 例如: Zookeeper)
     * @return
     */
    private T createExtension(String name) {
        // key: 实现类简称(字符串)
        // value: 实现类的 class 对象
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new RuntimeException("不存在的扩展实现类 " + name);
        }
        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        if (instance == null) {
            try {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            } catch (Exception e) {
                logger.error(e.getMessage());
                throw new RuntimeException("Fail to create an instance of the extension class " + clazz);
            }
        }
        return instance;
    }

    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classesMap = cachedClasses.get();
        if (classesMap == null) {
            synchronized (cachedClasses) {
                classesMap = cachedClasses.get();
                if (classesMap == null) {
                    classesMap = new HashMap<>();
                    // load all extensions from our extensions directory
                    loadDirectory(classesMap);
                    cachedClasses.set(classesMap);
                }
            }
        }
        return classesMap;
    }

    private void loadDirectory(Map<String, Class<?>> extensionClasses) {
        String fileName = ExtensionLoader.SERVICE_DIRECTORY + type.getName();
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourceUrl = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceUrl);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, URL resourceUrl) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), UTF_8))) {
            String line;
            // read every line
            while ((line = reader.readLine()) != null) {
                // get index of comment
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    // string after # is comment so we ignore it
                    line = line.substring(0, ci);
                }
                line = line.trim();
                if (line.length() > 0) {
                    try {
                        final int ei = line.indexOf('=');
                        // key: 实现类简称
                        String name = line.substring(0, ei).trim();
                        // value: 实现类全限定名
                        String clazzName = line.substring(ei + 1).trim();
                        // our SPI use key-value pair so both of them must not be empty
                        if (name.length() > 0 && clazzName.length() > 0) {
                            Class<?> clazz = classLoader.loadClass(clazzName);
                            extensionClasses.put(name, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        logger.error(e.getMessage());
                    }
                }

            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
