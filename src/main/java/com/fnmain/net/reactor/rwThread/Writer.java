package com.fnmain.net.reactor.rwThread;

import com.fnmain.exception.ExceptionType;
import com.fnmain.exception.FrameworkException;
import com.fnmain.net.config.WriterConfig;
import com.fnmain.net.enums.Constants;
import com.fnmain.net.reactor.node.IntMap;
import com.fnmain.net.reactor.node.WriterNode;
import com.fnmain.net.reactor.resource.Channel;
import com.fnmain.net.reactor.rwThread.threadType.WriterTask;

import java.lang.foreign.MemorySegment;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


public final class Writer {
    private static final AtomicInteger counter = new AtomicInteger(0);
    private final BlockingQueue<WriterTask> queue = new LinkedBlockingQueue<>();

    private final Thread writerThread;

    public Writer(Thread writerThread) {
        this.writerThread = writerThread;
    }

    public Thread thread() {
        return writerThread;
    }


    private Thread createWriterThread(WriterConfig writerConfig) {
        int sequence = counter.getAndIncrement();
        return Thread.ofPlatform().name(STR."Writer-\{sequence}").unstarted(()->{
            //队列事件处理
        });
    }

    public Writer(WriterConfig writerConfig) {
        this.writerThread = createWriterThread(writerConfig);
    }

    public void submit(WriterTask writerTask) {
        if (writerTask == null || !queue.offer(writerTask)) {
            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        }
    }

    private void processWriterTasks(IntMap<WriterNode> nodeMap, MemorySegment reversed) throws InterruptedException {
        while (true) {
            WriterTask writerTask = queue.take();

            switch (writerTask.type()) {
                case SINGLE_MSG -> handleSingleMsg(nodeMap, writerTask, reversed);
                case MULTIPLE_MSG -> handleMultipleMsg(nodeMap, writerTask, reversed);
            }

        }
    }


    private void handleSingleMsg(IntMap<WriterNode> nodeMap, WriterTask writerTask, MemorySegment reversed) {
        Channel channel = writerTask.channel();
        WriterNode writerNode = nodeMap.get(channel.socket().intValue());

        if (writerNode != null) {
            writerNode.onMsg(reversed, writerTask);
        }
    }

    private void handleMultipleMsg(IntMap<WriterNode> nodeMap, WriterTask writerTask, MemorySegment reversed) {
        Channel channel = writerTask.channel();
        WriterNode writerNode = nodeMap.get(channel.socket().intValue());

        if (writerNode != null) {
            writerNode.onMsg(reversed, writerTask);
        }
    }
}
