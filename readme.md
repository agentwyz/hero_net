Hero Net网络框架文档

下面是完整的架构图

<img src="https://i0.imgs.ovh/2024/03/17/e3Ubd.png" alt="e3Ubd.png" style="zoom: 50%;" />



1. 组件Net.class做为整个主线程,  

2. 为每一个TCP连接分配了一个channel资源, 并将TCP分为了两个阶段
3. Poller作为读线程, Wiriter作为写线程, 它们两个处理的对象叫做Node
4. TCP的两个阶段分别叫做Sentry和Protocol我们会将这两个阶段挂到对应的节点上去, 比如会将Sentry先挂载到PollerNode上面去, 当有写事件触发的时候我们会转移到ProtocolPollerNode上面去
5. 当挂到对应的节点上去的时候, 我们就可以将它提交到工作线程上面去, 
6. 类似于Netty的ReaderBuffer和WriterBuffer



### 源码分析

TCP的第一阶段是Sentry阶段, 是TCP的认证阶段, 在这个阶段中会进行下面这些变化

```java 
poller->sentryNode->protocol
```

主要是框架的的submit函数将对应的事件提交的对应的任务列表中:

```java 
ctl(scoket, 写事件);
poller.submit(pollerTask);

//在map中注册
map.put(socket, sentry);

//epoll监听获得写事件,
wait(写事件);

//如果监听到了写事件, 在map中查找socket对应的节点, 如果找到就说明之前注册写事件的socket连接成功
map.get(socket);

//连接成功之后, updateToProtocol
```

TCP的第二阶段是Protocol阶段, 是TCP的读写阶段, 下面这个函数是整个框架的核心

```java
private void updateToProtocol() {
    try {
        channel.handler().onConnected(channel); //连接逻辑的处理
    } catch (RuntimeException e) {
        System.out.println(STR."Err occurred in onConnected() \{e}");
        close();
        return;
    }
    //之前注册的写是为了挂载线程的, 现在我们只需要读事件了
    ctl(Constants.NET_R);
    //首先转换成为Protocol    
    Protocol protocol = sentry.toProtocol();
    ProtocolPollerNode pollerNode = new ProtocolPollerNode(nodeMap, channel, protocol, channelState);
    
    /*替换了sentry, 如果下次触发事件, 那么就会调用protocolPollerNode.onWriterAble()函数
    对应的就是TcpProtocol*/
    nodeMap.replace(channel.socket().intValue(), this, pollerNode);
    
    /*调用写线程*/
    channel.writer().submit(new WriterTask(WriterTaskType.INITIATE, channel,
            new ProtoAndState(protocol, channelState), null));
}
```

当执行了这段逻辑之后, 我们就会把map中的sentry更新为protocol, 同时调用writer写线程, 此时writer写线程就会处理它对应的节点

```java 
poller->pollerNode->prorocolPollerNode
writer->writerNode->protocolWriterNode
```

首先我们来看第一个链路:

```java
public void onReadableEvent(MemorySegment reserved, int len)  {
    int r;
    try {
        //调用对应的
        r = protocol.onReadableEvent(reserved, len);
    } catch (RuntimeException e) {
        close();
    }
 }
```

