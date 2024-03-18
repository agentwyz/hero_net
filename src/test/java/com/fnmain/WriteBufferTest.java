package com.fnmain;

import com.fnmain.buffer.WriteBuffer;
import com.fnmain.utils.NativeUtil;
import org.junit.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static org.junit.Assert.assertEquals;

public class WriteBufferTest {
    @Test
    public void should_write_4_byte() {
        try (WriteBuffer writeBuffer = WriteBuffer.newDefaultWriteBuffer(Arena.ofConfined(), 4)) {
            byte a = 1;
            byte b = 2;
            byte c = 3;
            byte d = 4;

            writeBuffer.writeByte(a);
            writeBuffer.writeByte(b);
            writeBuffer.writeByte(c);
            writeBuffer.writeByte(d);

            MemorySegment segment = writeBuffer.content();

            assertEquals(1, NativeUtil.getByte(segment, 0));
            assertEquals(2, NativeUtil.getByte(segment, 1));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
