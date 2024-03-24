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
import com.fnmain.net.reactor.rwThread.PollerTaskType;
import com.fnmain.net.reactor.rwThread.WriterCallback;
import com.fnmain.net.reactor.rwThread.threadType.PollerTask;
import com.fnmain.net.reactor.rwThread.threadType.WriterTask;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;


public final class ProtocolWriterNode implements WriterNode {

    private static final OsNetworkLibrary OS = OsNetworkLibrary.CURRENT;

    private record Task(Arena arena, MemorySegment memorySegment, WriterCallback writerCallback) {}

    private IntMap<WriterNode> nodeMap;

    private Channel channel;

    private final Protocol protocol;

    private final State channelState;

    private Duration duration;


    private Deque<Task> taskQueue;


    public ProtocolWriterNode(IntMap<WriterNode> nodeMap, Channel channel, Protocol protocol, State channelState) {
        this.nodeMap = nodeMap;
        this.channel = channel;
        this.protocol = protocol;
        this.channelState = channelState;
    }


    @Override
    public void onMsg(MemorySegment reserved, WriterTask writerTask) {
        if (writerTask.channel() == channel) {

            Object msg = writerTask.msg(); //首先获取对应的msg
            WriterCallback writerCallback = writerTask.writerCallback();

            try (final WriteBuffer writeBuffer = WriteBuffer.newResevedWriteBuffer(reserved)) {
                try {
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


    private void sendMsg(WriteBuffer writeBuffer, WriterCallback writerCallback) {
        MemorySegment data = writeBuffer.toSegment();

        if (taskQueue == null) {
            int len = (int) data.byteSize();
            int r; //对应的事件类型

            while (true) {
                try {
                    //TODO: 关于这点的写还没有弄明白
                    r = protocol.doWrite(data, len);

                    if (r > 0 && r < len) {
                        len = len - r;
                        data = data.asSlice(r, len);
                    } else {
                        break;
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    close();
                    return;
                }
            }

            if (r == len) {
                if (writerCallback != null) {
                    writerCallback.invokeOnSuccess(channel);
                }
            } else {
                taskQueue = new ArrayDeque<>();
                copyLocally(data, writerCallback);

                if (r < 0) {
                    handleEvent(r);
                }
            }

        } else { //没有完全发送

        }
    }

    private void handleEvent(int r) {
        if (r == Constants.NET_PW || r == Constants.NET_PR) {
            ctl(r);
        } else if (r != Constants.NET_IGNORED) {
            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        }
    }

    private void ctl(int r) {
        if(ctlWithStateChecked(expectedState(r))) {
            close();
        }
    }

    private int expectedState(int r) {
        return switch (r) {
            case Constants.NET_PR -> Constants.NET_W;
        }
    }


    private boolean ctlWithStateChecked(int expected) throws Exception {
        try (Mutex _ = channelState.withMutex()) {
            int state = channelState.get();

            if((state & Constants.NET_PC) == Constants.NET_PC) {
                return true;
            }
            int current = state & Constants.NET_RW;
            int to = current | expected;
            if(to != current) {
                OsNetworkLibrary.ctlMux(channel.poller().mux(), channel.socket(), current, to);
                channelState.set(state + (to - current));
            }
            return false;
        }
    }




    private void copyLocally(MemorySegment data, WriterCallback writerCallback) {
        Arena arena = Arena.ofConfined();
        long size = data.byteSize();
        MemorySegment memorySegment = arena.allocateArray(ValueLayout.JAVA_BYTE, size);
        MemorySegment.copy(memorySegment, 0, memorySegment, 0, size);
        taskQueue.addLast(new Task(arena, memorySegment, writerCallback));
    }


    public void close() {
        if (nodeMap.remove(channel.socket().intValue(), this)) {
            try (Mutex _ = channelState.withMutex()) {
                int current = channelState.get();
                channelState.set(current | Constants.NET_WC);

                if ((current & Constants.NET_PW) > 0) {
                    closeProtocol();
                } else {
                    channel.poller().submit(new PollerTask(PollerTaskType.CLOSE, channel, null));
                }


            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void closeProtocol() {
        try {
            protocol.doClose();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onMultipleMsg(MemorySegment reserved, WriterTask writerTask) {
        if (writerTask.channel() == channel && writerTask.msg() instanceof Collection<?> msgs) {
            WriterCallback writerCallback = writerTask;
        }
    }

    @Override
    public void onWriteable(WriterTask writerTask) {
        if (writerTask.channel() == channel) {
            while (true) {
                Task task = taskQueue.pollFirst();

                if (task == null) {

                }

                MemorySegment data = task.memorySegment;
                WriterCallback writerCallback = task.writerCallback;
                int len = (int) data.byteSize();

                int r;

                while (true) {
                    try {
                        r = protocol.doWrite(data, len);

                        if (r > 0 && r < len) {
                            len = len - r;
                            data = data.asSlice(r, len);
                        } else {

                        }
                    } catch () {

                    }
                }

            }
        }
    }

    @Override
    public void onMultipleMsg() {

    }

    @Override
    public void shoutDown(WriterTask writerTask) {

    }

    @Override
    public void shutDown(WriterTask writerTask) {
        if(writerTask.channel() == channel && writerTask.msg() instanceof Duration duration) {
            conditionalShutdown(duration);
        }
    }
}
