package com.fnmain.net.reactor.node;

import com.fnmain.net.reactor.rwThread.threadType.PollerTask;

import java.lang.foreign.MemorySegment;
import java.time.Duration;

public sealed interface PollerNode permits SentryPollerNode, ProtocolPollerNode {
    void onReadableEvent(MemorySegment reserved, int len) throws Exception;

    void onWriteableEvent();

    void onClose(PollerTask pollerTask);

    void exit(Duration duration);
}
