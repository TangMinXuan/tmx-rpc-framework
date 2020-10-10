# tmx-rpc-framework
一个简易的RPC框架，主要目的是学习阅读别人的代码，学习编写轮子，学习维护开源项目

## 启动
- 运行 example-server 模块中的 RpcServerExampleMain 启动 provider 并注册 HelloService 服务
- 运行 example-client 模块中的 RpcClientExampleMain 启动 consumer 并尝试调用 provider 中提供的 HelloService 服务

## v1.0
实现了最基本的远程调用功能，但是是使用原始的 socket(BIO) 实现，未来的版本将使用 netty(NIO) 去实现交互

## v2.0
1) 网络通信层由 socket 换成 netty
2) 采用 Kryo 作为序列化框架, 未来将支持自定义序列化框架
3) 重构代码结构

## v2.1
增加requestId