package com.fnmain.utils;

import com.fnmain.buffer.WriteBuffer;

public interface WriteBufferPolicy {
    void resize(WriteBuffer writeBuffer, long nextIndex);
    void close(WriteBuffer writeBuffer);
}


