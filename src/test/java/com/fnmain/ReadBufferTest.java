package com.fnmain;

import com.fnmain.exception.FrameworkException;
import com.fnmain.utils.NativeUtil;
import com.fnmain.buffer.ReadBuffer;
import org.junit.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ReadBufferTest {


    @Test
    public void should_allocate_a_byte() {
        try (Arena arena = Arena.ofConfined()) {
            byte a = 1;
            MemorySegment memorySegment = arena.allocate(ValueLayout.JAVA_BYTE, a);
            ReadBuffer readBuffer = new ReadBuffer(memorySegment);
            assertEquals(1, readBuffer.readByte());
        }
    }

    @Test
    public void should_allocate_a_int() {
        try (Arena arena = Arena.ofConfined()) {
            int a = 1;
            MemorySegment memorySegment = arena.allocate(ValueLayout.JAVA_INT, a);
            ReadBuffer readBuffer = new ReadBuffer(memorySegment);
            assertEquals(1, readBuffer.readInt());
        }
    }


    @Test
    public void should_allocate_a_short() {
        try (Arena arena = Arena.ofConfined()) {
            short a = 1;
            MemorySegment memorySegment = arena.allocate(ValueLayout.JAVA_SHORT, a);
            ReadBuffer readBuffer = new ReadBuffer(memorySegment);
            assertEquals(1, readBuffer.readShort());
        }
    }


    @Test
    public void should_throw_a_index_overflow_exception() {
        try (Arena arena = Arena.ofConfined()) {
            int a = 4;
            MemorySegment memorySegment = arena.allocate(ValueLayout.JAVA_INT, 4);
            ReadBuffer readBuffer = new ReadBuffer(memorySegment);
            assertThrows(FrameworkException.class, () -> {
                readBuffer.readInt();
                readBuffer.readInt();
            });
        }
    }


    @Test
    public void should_allocate_a_string() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment memorySegment = NativeUtil.allocateStr(arena, "hello, World");
            ReadBuffer readBuffer = new ReadBuffer(memorySegment);
            assertEquals("hello, World", readBuffer.readCStr());
        }
    }
}
