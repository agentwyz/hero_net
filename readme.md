Hero Net网络框架文档

下面是完整的架构图

<img src="https://i0.imgs.ovh/2024/03/17/e3Ubd.png" alt="e3Ubd.png" style="zoom: 50%;" />



1. Epoll+ 事件驱动+TCP作为整个框架的核心
2. 组件Net.class做为整个主线程,  
3. 为每一个TCP连接分配了一个channel资源, 并将TCP分为了两个阶段
4. Poller作为读线程, Wiriter作为写线程, 它们两个处理的对象叫做Node
5. TCP的两个阶段分别叫做Sentry和Protocol我们会将这两个阶段挂到对应的节点上去, 比如会将Sentry先挂载到PollerNode上面去, 当有写事件触发的时候我们会转移到ProtocolPollerNode上面去
6. 当挂到对应的节点上去的时候, 我们就可以将它提交到工作线程上面去, 
7. 类似于Netty的ReaderBuffer和WriterBuffer



### 源码分析

TCP的第一阶段是Sentry阶段, 是TCP的认证阶段, 在这个阶段中会进行下面这些变化

```java 
poller->sentryNode->pollerNode
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
    
    ProtocolPollerNode pollerNode = 
        new ProtocolPollerNode(nodeMap, channel, protocol, channelState);
    
    /*替换了sentry, 如果下次触发读事件*/
    nodeMap.replace(channel.socket().intValue(), this, pollerNode);
    
    /*调用写线程*/
    channel.writer().submit(new WriterTask(WriterTaskType.INITIATE, channel,
            new ProtoAndState(protocol, channelState), null));
}
```

当执行了这段逻辑之后, 我们就会把map中的sentry更新为protocol, 同时调用writer写线程, 此时writer写线程就会处理它对应的节点

```java 
poller->pollerNode->protocolPollerNode
writer->writerNode->protocolWriterNode
```

首先我们来看第一个链路, 第一个链路是在sentry转换为protocol之后完成的

```java
pollerNode.class

//reversed表示当前事件的指针, 这个数组是由wait函数获取的
MemorySegment reserved = reservedArray[index];

//1 当一旦触发可读事件的时候, 
public void onReadableEvent(MemorySegment reserved, int len)  {
    int r;
    try {
        r = protocol.onReadableEvent(reserved, len);
    } catch (RuntimeException e) {
        close();
    }
 }


TCP protocol.class

//2 protocol是一个接口, 它的对应实现就是TCP protocol
@Override
public int onReadableEvent(MemorySegment reserved, int len) {
    //使用操作系统底层的recv函数接收信息
    int r = OS.recv(channel.socket(), reserved, len);

    if (r < 0) {
        throw new FrameworkException(ExceptionType.NETWORK, STR."");
    } else {
        return r;
    }

}
```

接下来看第二个链路:

```java 
writerNode.class
@Override
public void onMsg(MemorySegment reserved, WriterTask writerTask) {
    //首先判断当前的writerTask中的chananel是不是channel中的
    if (writerTask.channel() == channel) {

        Object msg = writerTask.msg(); //首先获取对应的msg
        
        WriterCallback writerCallback = writerTask.writerCallback();

        try (final WriteBuffer writeBuffer 
             = WriteBuffer.newResevedWriteBuffer(reserved)) {
            try {
                //对数据进行编码
                channel.encoder().encode(writeBuffer, msg);
            } catch (RuntimeException e) {
                e.printStackTrace();
                return;
            }

            if (writeBuffer.writeIndex() > 0) {
                sendMsg(writeBuffer, writerCallback);
            }


            if (writerCallback != null) {
                writerCallback.invokeOnSuccess(channel);
            }
        }
    }
}
```

channel是整个网络框架的核心数据, 它包含了TCP连接必须品socket, 用于编码的eoncode, 用于解码的decode, 以及与其相关的线程

```java
public sealed interface Channel{
    //Socket对象
    Socket socket();

    //编码对象
    Encoder encoder();

    //解码对象
    Decoder decoder();

    //处理业务数据
    Handler handler();

    Poller poller(); //读线程

    Writer writer(); //写线程

    Loc loc();
}

```

底层buffer模型, 我们知道数据的都是有格式的, 比如http就是这样的一种格式的协议, 



socket对象上的事件

1. 读事件
2. 连接事件
3. 写事件



node上的事件:

1. sentrynode
2. protocolPollerNode