Hero Net网络框架文档

下面是完整的架构图

![e3Ubd.png](https://i0.imgs.ovh/2024/03/17/e3Ubd.png)



1. 组件Net.class做为整个主线程,  

2. 为每一个TCP连接分配了一个channel资源, 并将TCP分为了两个阶段
3. Poller作为读线程, Wiriter作为写线程, 它们两个处理的对象叫做Node
4. TCP的两个阶段分别叫做Sentry和Protocol我们会将这两个阶段挂到对应的节点上去, 比如会将Sentry先挂载到PollerNode上面去, 当有写事件触发的时候我们会转移到ProtocolPollerNode上面去
5. 当挂到对应的节点上去的时候, 我们就可以将它提交到工作线程上面去, 
6. 类似于Netty的ReaderBuffer和WriterBuffer