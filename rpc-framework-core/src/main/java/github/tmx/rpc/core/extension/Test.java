package github.tmx.rpc.core.extension;

import github.tmx.rpc.core.registry.ServiceRegistry;

/**
 * @author: TangMinXuan
 * @created: 2020/10/29 16:27
 */
public class Test {

    public static void main(String[] args) {
        ServiceRegistry serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("Zookeeper");
    }
}
