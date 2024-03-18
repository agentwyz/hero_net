package com.fnmain.net.reactor.process;

import com.fnmain.exception.ExceptionType;
import com.fnmain.exception.FrameworkException;
import com.fnmain.net.enums.Constants;
import com.fnmain.net.platform.OsNetworkLibrary;
import com.fnmain.net.reactor.resource.Channel;
import com.fnmain.net.reactor.resource.Socket;
import com.fnmain.net.reactor.rwThread.threadType.WriterTask;
import com.fnmain.net.reactor.rwThread.threadType.WriterTaskType;

import java.lang.foreign.MemorySegment;

public record TcpProtocol(Channel channel) implements Protocol {

    private static final OsNetworkLibrary OS = OsNetworkLibrary.CURRENT;

    @Override
    public int onReadableEvent(MemorySegment reserved, int len) {

        int r = OS.recv(channel.socket(), reserved, len);

        if (r < 0) {
            throw new FrameworkException(ExceptionType.NETWORK, STR."");
        } else {
            return r;
        }

    }


    @Override
    public int onWriteableEvent() {
        channel.writer().submit(new WriterTask(WriterTaskType.WRITABLE, channel, null, null));
        return Constants.NET_R;
    }



    @Override
    public int doWrite(MemorySegment data, int len) {
        Socket socket = channel.socket();

        int r = OS.send(socket, data, len);

        if (r < 0) {
            int errno = Math.abs(r);

            if (errno == OS.sendBlockCode()) {
                return Constants.NET_PW;
            } else {
                throw new FrameworkException(ExceptionType.NETWORK, STR."Failed to perform send()");
            }

        } else {
            return r;
        }

    }

    @Override
    public void doShutdown() {
        int r = OS.shutdownWrite(channel.socket());

        if (r < 0) {
            throw new FrameworkException(ExceptionType.NATIVE, STR."failed to perform shutdown(), errno:\{Math.abs(r)}");
        }

    }



    @Override
    public void doClose() {
        int r = OS.closeSocket(channel.socket());

        if (r < 0) {
            throw new FrameworkException(ExceptionType.NATIVE, STR."Failed to close socket, errno:\{Math.abs(r)}");
        }
    }
}
