package com.fnmain.net.reactor.process;

import com.fnmain.exception.ExceptionType;
import com.fnmain.exception.FrameworkException;
import com.fnmain.net.enums.Constants;
import com.fnmain.net.platform.OsNetworkLibrary;
import com.fnmain.net.reactor.resource.Channel;

import java.lang.foreign.MemorySegment;

public record TcpSentry(Channel channel) implements Sentry {

    private static final OsNetworkLibrary OS = OsNetworkLibrary.CURRENT;

    @Override
    public int onReadableEvent(MemorySegment reserved, int len) {

        //TCP的sentry在这个阶段是不会执行的
        throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
    }

    @Override
    public int onWriteableEvent() {
        int errorOpt = OS.getErrOpt(channel.socket());

        if (errorOpt == 0) {
            return Constants.NET_UPDATE;
        } else {
            throw new RuntimeException(STR."Failed to establish connection err opt, :\{errorOpt}");
        }
    }

    @Override
    public Protocol toProtocol() {
        return new TcpProtocol(channel);
    }

    @Override
    public void doClose() {
        int r = OS.closeSocket(channel.socket());

        if (r < 0) {
            throw new FrameworkException(ExceptionType.NETWORK, STR."Failed to close socket, error: Math.abs(r)");
        }
    }
}
