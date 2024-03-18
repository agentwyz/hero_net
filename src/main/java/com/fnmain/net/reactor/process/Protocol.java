package com.fnmain.net.reactor.process;

import java.lang.foreign.MemorySegment;

public interface Protocol {
    int onReadableEvent(MemorySegment reserved, int len);
    int onWriteableEvent();

    int doWrite(MemorySegment data, int len);


    void doShutdown();
    void doClose();
}
