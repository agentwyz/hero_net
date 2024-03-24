package com.fnmain.net.reactor.rwThread;

import com.fnmain.utils.NativeUtil;

import java.lang.foreign.MemorySegment;


public record Mux(MemorySegment winHandle, int epfd, int kqfd) {

    public static Mux linux(int epfd) {
        return new Mux(NativeUtil.NULL_POINTER, epfd, Integer.MIN_VALUE);
    }


    @Override
    public String toString() {
            
    }
}
