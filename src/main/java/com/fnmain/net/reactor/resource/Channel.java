package com.fnmain.net.reactor.resource;

import com.fnmain.net.reactor.rwThread.Poller;
import com.fnmain.net.reactor.rwThread.Writer;
import com.fnmain.net.reactor.rwThread.WriterCallback;

import java.time.Duration;
import java.util.Collection;


/*
Channel对象代表着为一条成功建立的TCP连接所分配的所有资源
 */

public sealed interface Channel permits ChannelImpl {
    //Socket对象
    Socket socket();

    //编码对象
    Encoder encoder();

    //解码对象
    Decoder decoder();

    //处理业务数据
    Handler handler();

    Poller poller();

    Writer writer();

    Loc loc();


    void sendMsg(Object msg, WriterCallback writerCallback);

    void sendMultipleMsg(Collection<Object> msgs, WriterCallback writerCallback);

    default void sendMsg(Object msg) {
        sendMsg(msg, null);
    }

    default void sendMultipleMsg(Collection<Object> msgs) {
        sendMultipleMsg(msgs, null);
    }

    Duration defaultShutdownDuration = Duration.ofSeconds(5);

}
