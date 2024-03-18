package com.fnmain.net.reactor.control;

import com.fnmain.net.enums.Constants;
import com.fnmain.utils.NativeUtil;

import java.lang.foreign.*;

public record Timeout (int val, MemorySegment ptr) {
    private static final StructLayout timespecLayout = MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withName("tv_sec"),
            ValueLayout.JAVA_LONG.withName("tv_nesc"));

    private static final long secOffset =
            timespecLayout.byteOffset(MemoryLayout.PathElement.groupElement("tv_sec"));

    private static final long nsecOffset =
            timespecLayout.byteOffset(MemoryLayout.PathElement.groupElement("tv_nsec"));


    public static Timeout of(Arena arena, int milliseconds) {
        switch (NativeUtil.osType()) {
            case Windows, Linux -> {
                return new Timeout(milliseconds, null);
            }
            case null, default -> throw new RuntimeException(Constants.UNREACHED);
        }
    }
}
