package com.fnmain.net.reactor.node;

import com.fnmain.buffer.WriteBuffer;
import com.fnmain.exception.ExceptionType;
import com.fnmain.exception.FrameworkException;
import com.fnmain.net.enums.Constants;
import com.fnmain.net.enums.State;
import com.fnmain.net.platform.OsNetworkLibrary;
import com.fnmain.net.reactor.control.Mutex;
import com.fnmain.net.reactor.resource.Channel;
import com.fnmain.net.reactor.process.Protocol;
import com.fnmain.net.reactor.rwThread.threadType.PollerTask;

import java.lang.foreign.MemorySegment;
import java.time.Duration;


public final class ProtocolPollerNode implements PollerNode {

    private final IntMap<PollerNode> nodeMap;
    private final State channelState;
    private final Channel channel;
    private final Protocol protocol;

    private WriteBuffer tempBuffer;

    private static final OsNetworkLibrary os = OsNetworkLibrary.CURRENT;


    public ProtocolPollerNode(IntMap<PollerNode> nodeMap, Channel channel, Protocol protocol, State chananelState) {
        this.nodeMap = nodeMap;
        this.channel = channel;
        this.protocol = protocol;
        this.channelState = chananelState;
    }


    private void handleEvent(int r) {
        if (r == Constants.NET_W || r == Constants.NET_R) {
            ctl(r);
        } else if (r != Constants.NET_IGNORED) {
            throw new FrameworkException(ExceptionType.NETWORK, )
        }
    }


    @Override
    public void onReadableEvent(MemorySegment reserved, int len)  {
        int r;

        try {
            r = protocol.onReadableEvent(reserved, len);
        } catch (RuntimeException e) {
            close();
        }

    }

    @Override
    public void onWriteableEvent() {

    }

    @Override
    public void onClose(PollerTask pollerTask) {

    }

    @Override
    public void exit(Duration duration) {

    }

    private void ctl(int expected)  {
        try (Mutex _ = channelState.withMutex()) {
            Mutex state = channelState.withMutex();


        } catch (Exception e) {

        }
    }



    private void close()  {
        if (nodeMap.remove(channel.socket().intValue(), this)) {
            if (tempBuffer != null) {
                tempBuffer.close();
                tempBuffer = null;
            }


        }
    }
}
