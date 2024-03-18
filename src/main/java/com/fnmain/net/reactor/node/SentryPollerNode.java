package com.fnmain.net.reactor.node;

import com.fnmain.net.enums.Constants;
import com.fnmain.net.enums.State;
import com.fnmain.net.platform.OsNetworkLibrary;
import com.fnmain.net.reactor.resource.Channel;
import com.fnmain.net.reactor.process.Protocol;
import com.fnmain.net.reactor.rwThread.threadType.PollerTask;
import com.fnmain.net.reactor.process.Sentry;
import com.fnmain.net.reactor.rwThread.threadType.WriterTask;
import com.fnmain.net.reactor.rwThread.threadType.WriterTaskType;

import java.lang.foreign.MemorySegment;
import java.time.Duration;

public final class SentryPollerNode implements PollerNode {

    private Sentry sentry;
    private final Channel channel;
    private final IntMap<PollerNode> nodeMap;
    private final Runnable callback;
    private final State channelState = new State(Constants.NET_W);
    private final static OsNetworkLibrary os = OsNetworkLibrary.CURRENT;


    public SentryPollerNode(Sentry sentry, Channel channel, IntMap<PollerNode> nodeMap, Runnable callback) {
        this.sentry = sentry;
        this.channel = channel;
        this.nodeMap = nodeMap;
        this.callback = callback;
    }


    private void handleEvent(int t) {
        if (t == Constants.NET_UPDATE) {
            updateToProtocol();
        } else if (t == Constants.NET_R || t == Constants.NET_W || t == Constants.NET_RW) {
            ctl(t);
        }
    }

    private void ctl(int expected) {

       int current = channelState.get();

       if (current != expected) {
            //注册对应的状态
            os.ctlMux(channel.poller().mux(), channel.socket(), current, expected);
            channelState.set(expected);
       }
    }


    private void close() {

    }


    @Override
    public void onReadableEvent(MemorySegment reserved, int len) {
        try {
            handleEvent(sentry.onReadableEvent(reserved, len));
        } catch (RuntimeException e) {
            close();
        }
    }

    @Override
    public void onWriteableEvent() {
        try {
            handleEvent(sentry.onWriteableEvent());
        } catch (RuntimeException e) {
            close();
        }
    }

    @Override
    public void onClose(PollerTask pollerTask) {
        if (pollerTask.channel() == channel) {
            close();
        }
    }



    private void closeSentry() {
        try {
            sentry.doClose();
        } catch (RuntimeException e) {

        }

        if (callback != null) {
            Thread.ofVirtual().start(callback);
        }
    }




    private void updateToProtocol() {


        try {
            channel.handler().onConnected(channel);
        } catch (RuntimeException e) {
            System.out.println(STR."Err occurred in onConnected() \{e}");
            close();
            return;
        }

        ctl(Constants.NET_R);

        Protocol protocol = sentry.toProtocol();

        //创建一个Protocol节点
        ProtocolPollerNode pollerNode = new ProtocolPollerNode(nodeMap, channel, protocol, channelState);

        //将原来的Sentry节点更新为Protocol
        nodeMap.replace(channel.socket().intValue(), this, pollerNode);

        //并在writer中绑定对应的节点
        channel.writer().submit(
                new WriterTask(WriterTaskType.INITIATE, channel, new ProtoAndState(protocol, channelState), null));
    }


    @Override
    public void exit(Duration duration) {
        close();
    }
}
