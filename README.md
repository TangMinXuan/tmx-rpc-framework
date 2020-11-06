# tmx-rpc-framework

### 简单演示

### 自我介绍
tmx-rpc-framework 是一款基于 Netty 实现的简易 RPC 框架, 致力于让调用远程方法如同调用本地方法一样自然, 优雅. 
为了更好地屏蔽底层逻辑, 他支持使用注解的方式进行服务的发现与注册. 此外, 一些重要的组件, 
例如说: 注册中心, 序列化框架, 服务容器等, 写成了扩展点(SPI), 这样以后更换起这些组件就更加方便了

### 编写动机
写这个项目的动机来自于我在实习过程中的一次性能调优. 
那个场景是, 我的代码需要循环的远程调用另一个基础组件后做相应的逻辑. 由于两端通信的方式是基于 HTTP + JSON , 
最终的结果是, 在循环次数过大的时候性能非常拖沓. 
虽然业界已经有非常多优秀的开源 RPC 框架了, 例如: 阿里巴巴的 Dubbo, Google 的 gRPC ... 
但是本着提升自己, 学习造轮子, 努力成为一名开源人的目的, 我在实习结束后
便萌生了自己写一个简易能跑的 RPC 框架的想法. 虽说 RPC 的理论知识不难, 但在实现的过程中, 我也遇到了许多的问题. 
通过不断的尝试去解决这些问题, 我了解到了不少 RPC 框架底层原理以及各种 Java 编码技巧.  
> 在下只是一名普通二本大学的学生, 想必项目中存在不少地方写的不尽人意, 希望各位前辈不吝赐教

### 如何使用
tmx-rpc-framework 使用起来非常简单, 实际上, 您只需要使用简单的几个注解, 就能使用 RPC 服务  
步骤1. 使用 `@EnableRPC` 注解标记启动类  
   ```java
   @EnableRPC
   public class RpcServerApplication {
       public static void main(String[] args) throws IOException {
           ApplicationContext ctx = new AnnotationConfigApplicationContext(RpcServerApplication.class);
           System.in.read();
       }
   }
   ```
步骤2. 使用 `@RpcService` 标记实现了接口的服务提供类, 在这里 `HelloService` 是暴露的接口
   ```java
   @RpcService
   public class HelloServiceImpl implements HelloService {
       @Override
       public Hello sayHello(Hello hello){
           // 您的实现
       }
   }
   ```
步骤3. 使用 `@RpcReference` 标记暴露接口的引用
   ```java
    public class HelloController {
        @RpcReference
        HelloService helloService;
        
        // 直接使用 helloService 这个引用来调用需要的方法
    }
   ```


### 重要组件
- Netty 是网络通信组件, 为上层提供 NIO 的通信方式, 此外, 双向心跳机制和对通信协议的封装也是基于 Netty 来实现的.

- Zookeeper 是作为框架的注册中心, 得益于 Zookeeper 的 Watcher 设计, 服务提供者(provider)在注册与注销服务时, 
订阅相应接口的服务消费者(consumer)都能得到及时的反馈. 

- Protostuff 是默认的序列化组件, Protostuff 是由 Protobuf 改进来的, 选择 Protostuff 的一个很重要原因是如果不考虑跨语言, 
Protostuff 的序列化/反序列化速度在一众序列化组件中名列前茅, 我还适配了 Kryo , Kryo 的特点是序列化后的包体积比较小. 能减少网络开销. 

- Spring 是作为默认的服务容器来使用, 当然, "扫描注解" 这个功能也交给了擅长扫描 Bean 的 Spring 来实现

### 整体结构
```
tmx-rpc-framework
  ├─example-...     - 一些 demo , 方便用户快速了解如何使用这个框架
  └─rpc-framework-core
      ├─common      - 公共部分, 包括一些异常的定义, 一些枚举信息, 工具类等
      ├─config      - 框架配置, 在这里实现读取用户的配置信息(rpc.properties)
      ├─container   - 服务容器, 存储 provider 发布的服务实现类
      ├─extension   - 扩展点(SPI), 决定一些重要组件的实现类
      ├─loadbalance - 负载均衡, 包括负载均衡接口和内置策略
      ├─netty       - 通信组件, 包括心跳机制, 反射调用等都是在这实现
      │  ├─client   - 客户端
      │  ├─codec    - 编解码器
      │  └─server   - 服务端
      ├─registry    - 注册中心, 默认使用 Zookeeper 实现了服务注册与发现
      ├─serialize   - 序列化组件, 为编解码器提供序列化/反序列化服务
      └─spring      - Spring 相关组件, 主要包括注解扫描, Bean 的扫描与注册等
```

### 可配置项
一个优秀的框架应该是灵活可配置的, 下面例举了一部分可配置项, 更多配置项可去 `config/ConfigurationEnum.java` 中查看
```
# 注册中心地址
rpc.zookeeper.address = 119.23.235.40:2181

# Zookeeper 根目录
rpc.zookeeper.rootPath = /tmx-rpc

# 连接 Zookeeper 失败时的重试次数
rpc.zookeeper.retryCount = 3

# 服务器监听端口
rpc.server.port = 9999
# 服务器最大容忍客户端停顿时间, 若超过这个时间未收到客户端消息, 主动关闭连接 (单位: 秒)
rpc.server.maxPauseTime = 30

# 客户端 PING 的数量达到这个值时主动关闭连接
rpc.netty.heartbeatThreshold = 3

# 客户端最大容忍服务器响应时间, 若超过这个时间未得到服务器响应则认为服务器宕机, 主动关闭连接 (单位: 秒)
rpc.client.maxPauseTime = 10

# 客户端连接服务端失败时的重试次数
rpc.client.retryCount = 3

# 客户端负载均衡策略
rpc.client.loadBalance = Random

# 序列化组件
rpc.serializer = Protostuff
```


### 未来演变
- [x] 使用 Netty（基于 NIO）作为底层网络通信
- [x] 使用 Kryo 作为序列化框架
- [x] 使用 Zookeeper 作为注册中心, 管理服务提供者的地址信息
- [x] 使用 Map 缓存已经建立的 Channel 避免重复连接服务端
- [x] 使用 CompletableFuture 异步接收RPC执行结果
- [x] 增加 Netty 双向心跳机制
- [x] 使用注解进行服务注册(提供者)和服务消费(消费者)
- [x] 适配 Spring
- [x] 集成 Spring Boot
- [x] 支持读取配置文件(rpc.properties)配置文件来配置
- [x] 支持简易的 SPI
- [x] 序列化方式可配置
- [x] 客户端选择服务提供者的时候进行负载均衡 (发布服务的时候增加 一个 loadbalance 参数)
- [x] 增加服务分组和版本号 (version, group)
- [x] 自己写一个网络通信协议, 原有的 RpcRequest 和 RpcRequest 对象作为消息体
  - 魔数: 4 个字节, 用来服务端筛选有效报文
  - 序列化框架编号: 与客户端商量好序列化方式
  - 消息体长度
- [ ] ......  