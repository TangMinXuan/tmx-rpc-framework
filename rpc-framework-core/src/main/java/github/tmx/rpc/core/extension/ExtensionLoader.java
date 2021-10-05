package github.tmx.rpc.core.extension;

import github.tmx.rpc.core.common.utils.Compiler;
import github.tmx.rpc.core.common.utils.JavassistCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
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

    /**
     * key: @SPI接口的 class 对象
     * value: ExtensionLoader对象
     */
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();

    /**
     * key: 实现类的 class 对象
     * value: 实现类实例对象
     */
    private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    /**
     * 接口类 class 对象
     */
    private final Class<?> type;

    /**
     * key: 实现类简称
     * value: 实现类实例对象
     */
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    /**
     * key: 实现类简称(字符串)
     * value: 实现类的 class 对象
     */
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    // ================================= Adaptive 相关属性 =================================
    /**
     * TeaMaker$Adaptive 的实例对象（instance）缓存
     */
    private final Holder<Object> cachedAdaptiveInstance = new Holder<>();

    /**
     * TeaMaker$Adaptive 的Class对象缓存
     */
    private Class<?> cachedAdaptiveClass = null;


    // ================================= method =================================
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


    // ================================= 主动指定接口的实现类 =================================
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
                // Class -> instance , 并将生成的instance塞入缓存中
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            } catch (Exception e) {
                logger.error(e.getMessage());
                throw new RuntimeException("创建扩展类实例对象失败: " + clazz);
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
                    classesMap = new HashMap<>(16);
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
            Enumeration<java.net.URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    java.net.URL resourceUrl = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceUrl);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     *
     * @param extensionClasses
     * @param classLoader
     * @param resourceUrl
     */

    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, java.net.URL resourceUrl) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), UTF_8))) {
            String line;
            // 遍历配置文件每一行
            while ((line = reader.readLine()) != null) {
                // 获取注释的坐标
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    // ‘#’作为注释，我们忽略
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


    // ================================= 根据url动态决定接口实现类（Aaptive机制） =================================
    public T getAdaptiveExtension() {
        Object instance = cachedAdaptiveInstance.get();
        if (instance == null) {
            instance = createAdaptiveExtension();
            cachedAdaptiveInstance.set(instance);
        }
        return (T) instance;
    }

    private T createAdaptiveExtension() {
        try {
            return (T) getAdaptiveExtensionClass().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Can not create adaptive extension " + type + ", cause: " + e.getMessage(), e);
        }
    }

    private Class<?> getAdaptiveExtensionClass() {
        if (cachedAdaptiveClass != null) {
            return cachedAdaptiveClass;
        }
        cachedAdaptiveClass = createAdaptiveExtensionClass();
        return cachedAdaptiveClass;
    }

    private Class<?> createAdaptiveExtensionClass() {
        String code = createAdaptiveExtensionClassCode();
        ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
        Compiler compiler = new JavassistCompiler();
        return compiler.compile(code, classLoader);
    }

    private String createAdaptiveExtensionClassCode() {
        StringBuilder codeBuilder = new StringBuilder();
        Method[] methods = type.getMethods();

        // 是否有@Adaptive注解
        boolean hasAdaptiveAnnotation = false;
        for (Method m : methods) {
            if (m.isAnnotationPresent(Adaptive.class)) {
                hasAdaptiveAnnotation = true;
                break;
            }
        }
        if (!hasAdaptiveAnnotation) {
            throw new IllegalStateException("无法在接口 " + type.getName() + "上找到标记为@Adaptive的方法");
        }

        // 类的通用信息
        codeBuilder.append("package ").append(type.getPackage().getName()).append(";");
        codeBuilder.append("\nimport ").append(ExtensionLoader.class.getName()).append(";");
        codeBuilder.append("\npublic class ").append(type.getSimpleName()).append("$Adaptive").append(" implements ").append(type.getCanonicalName()).append(" {");

        for (Method method : methods) {
            Class<?> rt = method.getReturnType();
            Class<?>[] pts = method.getParameterTypes();
            Class<?>[] ets = method.getExceptionTypes();

            Adaptive adaptiveAnnotation = method.getAnnotation(Adaptive.class);
            StringBuilder code = new StringBuilder(512);
            // 对一个没有标注@Adaptive的方法，抛出异常
            if (adaptiveAnnotation == null) {
                code.append("throw new UnsupportedOperationException(\"method ")
                        .append(method.toString()).append(" of interface ")
                        .append(type.getName()).append(" 没有标注@Adaptive注解\");");
            } else {
                int urlTypeIndex = -1;
                for (int i = 0; i < pts.length; ++i) {
                    if (pts[i].equals(URL.class)) {
                        urlTypeIndex = i;
                        break;
                    }
                }
                // 成功找到方法入参数组中URL的位置
                if (urlTypeIndex != -1) {
                    // Null Point check
                    String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"url == null\");", urlTypeIndex);
                    code.append(s);

                    s = String.format("\n%s url = arg%d;", URL.class.getName(), urlTypeIndex);
                    code.append(s);
                } else {

                }

                // 使用接口名生成 决定接口的key
                char[] charArray = type.getSimpleName().toCharArray();
                StringBuilder sb = new StringBuilder(128);
                for (int i = 0; i < charArray.length; i++) {
                    if (Character.isUpperCase(charArray[i])) {
                        if (i != 0) {
                            sb.append(".");
                        }
                        sb.append(Character.toLowerCase(charArray[i]));
                    } else {
                        sb.append(charArray[i]);
                    }
                }
                String key = sb.toString();
                String getNameCode = String.format("url.getParameter(\"%s\", \"%s\")", key, null);

                code.append("\nString extName = ").append(getNameCode).append(";");
                // check extName == null?
                String s = String.format("\nif(extName == null) " +
                                "throw new IllegalStateException(\"Fail to get extension(%s) name from url(\" + url.toString() + \") use keys(%s)\");",
                        type.getName(), key);
                code.append(s);

                s = String.format("\n%s extension = (%<s)%s.getExtensionLoader(%s.class).getExtension(extName);",
                        type.getName(), ExtensionLoader.class.getSimpleName(), type.getName());
                code.append(s);

                // return statement
                if (!rt.equals(void.class)) {
                    code.append("\nreturn ");
                }

                s = String.format("extension.%s(", method.getName());
                code.append(s);
                for (int i = 0; i < pts.length; i++) {
                    if (i != 0)
                        code.append(", ");
                    code.append("arg").append(i);
                }
                code.append(");");
            }

            codeBuilder.append("\npublic ").append(rt.getCanonicalName()).append(" ").append(method.getName()).append("(");
            for (int i = 0; i < pts.length; i++) {
                if (i > 0) {
                    codeBuilder.append(", ");
                }
                codeBuilder.append(pts[i].getCanonicalName());
                codeBuilder.append(" ");
                codeBuilder.append("arg").append(i);
            }
            codeBuilder.append(")");
            if (ets.length > 0) {
                codeBuilder.append(" throws ");
                for (int i = 0; i < ets.length; i++) {
                    if (i > 0) {
                        codeBuilder.append(", ");
                    }
                    codeBuilder.append(ets[i].getCanonicalName());
                }
            }
            codeBuilder.append(" {");
            codeBuilder.append(code.toString());
            codeBuilder.append("\n}");
        }
        codeBuilder.append("\n}");
        if (logger.isDebugEnabled()) {
            logger.debug(codeBuilder.toString());
        }
        return codeBuilder.toString();
    }
}
