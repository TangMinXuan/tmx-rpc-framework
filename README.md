# tmx-rpc-framework
一个简易的RPC框架，主要目的是学习阅读别人的代码，学习编写轮子，学习维护开源项目

## v1.0
实现了最基本的远程调用功能，但是是使用原始的 socket(BIO) 实现，未来的版本将使用 netty(NIO) 去实现交互

## v2.0
1) 网络通信层由 socket 换成 netty
2) 采用 Kryo 作为序列化框架, 未来将支持自定义序列化框架
3) 重构代码结构

## v2.1
增加requestId, 方便以后拓展

## v2.2
1) 增加客户端重连功能
2) 增加公共接口中来往数据, 更方便看到效果

## v3.0
增加 Zookeeper 作为注册中心

## To-do-list
- [x] 使用 Netty（基于 NIO）作为底层网络通信
- [x] 使用 Kryo 作为序列化框架
- [x] 使用 Zookeeper 作为注册中心, 管理服务提供者的地址信息
- [x] 使用 Map 缓存已经建立的 Channel 避免重复连接服务端
- [x] 使用 CompletableFuture 异步接收RPC执行结果
- [x] 增加 Netty 双向心跳机制
- [x] 使用注解进行服务注册(提供者)和服务消费(消费者)
- [x] 适配 Spring
- [x] 集成 Spring Boot
- [ ] 序列化方式可配置
- [ ] 客户端选择服务提供者的时候进行负载均衡 (发布服务的时候增加 一个 loadbalance 参数)
- [ ] 增加服务版本号 
- [ ] 自己写一个网络通信协议, 原有的 RpcRequest 和 RpcRequest 对象作为消息体
  - 魔数: 4 个字节, 用来服务端筛选有效报文
  - 序列化框架编号: 与客户端商量好序列化方式, Kryo 还是 Json
  - 消息体长度
  - ......
