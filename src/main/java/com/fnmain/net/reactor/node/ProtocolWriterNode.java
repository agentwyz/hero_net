package com.fnmain.net.reactor.node;

import com.fnmain.net.enums.State;
import com.fnmain.net.platform.OsNetworkLibrary;
import com.fnmain.net.reactor.resource.Channel;
import com.fnmain.net.reactor.process.Protocol;
import com.fnmain.net.reactor.rwThread.WriterCallback;
import com.fnmain.net.reactor.rwThread.threadType.WriterTask;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.time.Duration;
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
    public void onMsg(MemorySegment reserved, WriterTask writerNode) {
        
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
