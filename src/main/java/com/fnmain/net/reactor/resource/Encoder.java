package com.fnmain.net.reactor.resource;

import com.fnmain.buffer.WriteBuffer;

@FunctionalInterface
public interface Encoder {
    void encode(WriteBuffer writeBuffer, Object o);
}
