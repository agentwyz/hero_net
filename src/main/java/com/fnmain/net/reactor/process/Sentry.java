package com.fnmain.net.reactor.process;



import java.lang.foreign.MemorySegment;


public interface Sentry{
    int onReadableEvent(MemorySegment reserved, int len);
    int onWriteableEvent();
    Protocol toProtocol();
    void doClose();
}
