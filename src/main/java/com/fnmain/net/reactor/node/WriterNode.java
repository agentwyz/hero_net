package com.fnmain.net.reactor.node;

import com.fnmain.net.reactor.rwThread.threadType.WriterTask;
import java.lang.foreign.MemorySegment;

public sealed interface WriterNode permits ProtocolWriterNode {

    void onMsg(MemorySegment reserved, WriterTask writerNode);

    void onMultipleMsg(MemorySegment reserved, WriterTask writerTask);

    void onWriteable(WriterTask writerTask);

    void shoutDown(WriterTask writerTask);
}
