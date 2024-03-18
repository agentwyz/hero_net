package com.fnmain.net.reactor.resource;

import com.fnmain.buffer.WriteBuffer;

@FunctionalInterface
public interface Decoder {
    void decode(WriteBuffer writeBuffer, Object o);
}
