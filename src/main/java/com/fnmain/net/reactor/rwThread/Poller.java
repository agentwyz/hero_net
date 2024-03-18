package com.fnmain.net.reactor.rwThread;

import com.fnmain.exception.ExceptionType;
import com.fnmain.exception.FrameworkException;
import com.fnmain.net.config.PollerConfig;
import com.fnmain.net.enums.Constants;
import com.fnmain.net.platform.IntPair;
import com.fnmain.net.platform.OsNetworkLibrary;
import com.fnmain.net.reactor.control.SentryWithCallback;
import com.fnmain.net.reactor.control.Timeout;
import com.fnmain.net.reactor.resource.Channel;
import com.fnmain.net.reactor.node.IntMap;
import com.fnmain.net.reactor.node.PollerNode;
import com.fnmain.net.reactor.node.SentryPollerNode;
import com.fnmain.net.reactor.process.Sentry;
import com.fnmain.net.reactor.rwThread.threadType.PollerTask;
import org.jctools.queues.atomic.MpscUnboundedAtomicArrayQueue;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;


public final class Poller {
    private final Thread pollerThread;
    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final OsNetworkLibrary osNetWorkLibrary = OsNetworkLibrary.CURRENT;
    private static final Mux mux = osNetWorkLibrary.createMux();

    private final Queue<PollerTask> readerTaskQueue = new MpscUnboundedAtomicArrayQueue<>(Constants.KB);

    public Mux mux() {
        return mux;
    }

    private Thread createPollerThread(PollerConfig pollerConfig) {
        int sequence = counter.getAndIncrement();

        return Thread.ofPlatform().name(STR."poller-\{sequence}").unstarted(()->{
            IntMap<PollerNode> nodeIntMap = new IntMap<>(pollerConfig.getMapSize());

            try (Arena arena = Arena.ofConfined()) {

                Timeout timeout = Timeout.of(arena, pollerConfig.getMuxTimeout());

                int maxEvents = pollerConfig.getMaxEvents();

                int readBufferSize = pollerConfig.getReadBufferSize();

                MemorySegment events = arena.allocate(MemoryLayout.sequenceLayout(maxEvents, osNetWorkLibrary.eventLayout()));

                MemorySegment[] reservedArray = new MemorySegment[maxEvents];


                for (int i = 0; i < reservedArray.length; i++) {

                    reservedArray[i] = arena.allocateArray(ValueLayout.JAVA_BYTE, readBufferSize);
                }

                int state = Constants.RUNNING;


                while (true) {
                    //等待处理事件
                    int count = osNetWorkLibrary.muxWait(mux, events, maxEvents, timeout);

                    if (count < 0) {
                        int errno = Math.abs(count);

                        if (errno == osNetWorkLibrary.interruptCode()) {
                            return;
                        } else {
                            throw new FrameworkException(ExceptionType.NETWORK, STR."Mux wait failed");
                        }

                    }

                    state = processTasks(nodeIntMap, state);

                    if (state == Constants.STOPPED) {
                        break;
                    }

                    for (int index = 0; index < count; index++) {

                        MemorySegment reserved = reservedArray[index];

                        IntPair pair = osNetWorkLibrary.access(events, index);

                        PollerNode pollerNode = nodeIntMap.get(pair.first());


                        if (pollerNode != null) {
                            int event = pair.second();

                            if (event == Constants.NET_W) {
                                pollerNode.onWriteableEvent();
                            } else if (event == Constants.NET_R) {
                                pollerNode.onReadableEvent(reserved, readBufferSize);
                            } else {
                                throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
                            }
                        }
                    }
                }
            }
        });
    }

    


    private int processTasks(IntMap<PollerNode> nodeMap, int currentState) {

        //这个就是一个事件驱动

        while (true) {
            PollerTask pollerTask = readerTaskQueue.poll();

            if (pollerTask == null) {
                return currentState;
            }

            switch (pollerTask.type()) {
                case BIND -> handleBindMsg(nodeMap, pollerTask);
            }
        }
    }

    private void handleBindMsg(IntMap<PollerNode> nodeMap, PollerTask pollerTask) {
        Channel channel = pollerTask.channel();

        SentryPollerNode sentryPollerNode = switch (pollerTask.msg()) {
            case Sentry sentry ->
                    new SentryPollerNode(sentry, channel, nodeMap,null);
            case SentryWithCallback sentryWithCallback ->
                    new SentryPollerNode(sentryWithCallback.sentry(), channel, nodeMap, sentryWithCallback.runnable());
            default ->
                    throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        };

        nodeMap.put(channel.socket().intValue(), sentryPollerNode);
    }




    public Thread thread() {
        return pollerThread;
    }


    public Poller(Thread pollerThread) {
        this.pollerThread = pollerThread;
    }

    public Poller(PollerConfig pollerConfig) {
        this.pollerThread = createPollerThread(pollerConfig);
    }


    public void submit(PollerTask pollerTask) {
        if (pollerTask == null || !readerTaskQueue.offer(pollerTask)) {
            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        }
    }




}
