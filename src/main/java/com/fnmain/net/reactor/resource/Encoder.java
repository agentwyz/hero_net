package com.fnmain.net.reactor.resource;

import com.fnmain.buffer.ReadBuffer;

import java.util.List;

@FunctionalInterface
public interface Encoder {
    void encode(ReadBuffer readBuffer, List<Object> entityList);
}
