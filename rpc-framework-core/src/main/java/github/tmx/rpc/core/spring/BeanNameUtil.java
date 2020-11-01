package github.tmx.rpc.core.spring;

import github.tmx.rpc.core.spring.annotion.RpcService;

/**
 * @author: TangMinXuan
 * @created: 2020/11/01 17:07
 */
public class BeanNameUtil {

    public static String getBeanName(Object service) {
        // 获取注解上的 group 和 version
        if (!service.getClass().isAnnotationPresent(RpcService.class)) {
            return null;
        }
        if (service.getClass().getInterfaces().length < 1) {
            return null;
        }
        String group = service.getClass().getAnnotation(RpcService.class).group();
        String version = service.getClass().getAnnotation(RpcService.class).version();
        String interfaceName = service.getClass().getInterfaces()[0].getCanonicalName();
        return getBeanName(interfaceName, group, version);
    }

    public static String getBeanName(String interfaceName, String group, String version) {
        return interfaceName + "[" + group + "]" + "[" + version + "]";
    }
}
