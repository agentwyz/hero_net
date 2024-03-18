package com.fnmain.buffer;

import com.fnmain.exception.ExceptionType;
import com.fnmain.exception.FrameworkException;
import com.fnmain.net.enums.Constants;
import com.fnmain.utils.NativeUtil;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;


public final class ReadBuffer {
    private final MemorySegment segment;
    private final long size;
    private long readerIndex;


    public ReadBuffer(MemorySegment segment) {
        this.segment = segment;
        this.size = segment.byteSize();
        this.readerIndex = 0L;
    }

    public long size() {
        return size;
    }

    public long readIndex() {
        return readerIndex;
    }


    public void setReaderIndex(long index) {
        if (index < 0 || index > size) {
            throw new FrameworkException(ExceptionType.NATIVE, "ReadIndex out of bound");
        }

        readerIndex = index;
    }

    public byte readByte() {
        long nextIndex = readerIndex + 1;

        if (nextIndex > size) {
            throw new FrameworkException(ExceptionType.NATIVE, "read index overflow");
        }

        byte b = NativeUtil.getByte(segment, readerIndex);
        readerIndex = nextIndex;
        return b;
    }

    /*读取一段count长度的字节数组到Java的堆内存中*/
    public byte[] readBytes(int count) {
        long nextIndex = readerIndex + count;

        if (nextIndex > size) {
            throw new FrameworkException(ExceptionType.NATIVE, "read index overflow");
        }

        byte[] result = segment.asSlice(readerIndex, count).toArray(ValueLayout.JAVA_BYTE);
        readerIndex = nextIndex;
        return result;
    }

    public short readShort() {
        long nextIndex = readerIndex + 2;
        if (nextIndex > size) {
            throw new FrameworkException(ExceptionType.NATIVE, "read index overflow");
        }

        short s = NativeUtil.getShort(segment, readerIndex);
        readerIndex = nextIndex;
        return s;
    }

    public int readInt() {
        long nextIndex = readerIndex + 4;

        if (nextIndex > size) {
            throw new FrameworkException(ExceptionType.NATIVE, "read index overflow");
        }

        int b = NativeUtil.getInt(segment, readerIndex);
        readerIndex = nextIndex;
        return b;
    }

    public long readLong() {
        long nextIndex = readerIndex + 8;

        if (nextIndex > size) {
            throw new FrameworkException(ExceptionType.NATIVE, "read index overflow");
        }

        long l = NativeUtil.getLong(segment, readerIndex);
        readerIndex = nextIndex;
        return l;
    }

    public MemorySegment readSegment(long count) {
        long nextIndex = readerIndex + count;

        if (nextIndex > size) {
            throw new FrameworkException(ExceptionType.NATIVE, "read index overflow");
        }

        MemorySegment result = segment.asSlice(readerIndex, count);
        readerIndex = nextIndex;

        return result;
    }

    public MemorySegment readHeapSegment(long count) {
        MemorySegment m = readSegment(count);

        if (m.isNative()) {
            long len = m.byteSize();
            byte[] bytes = new byte[(int) len];

            MemorySegment h = MemorySegment.ofArray(bytes);
            MemorySegment.copy(m, 0, h, 0, len);
            return h;
        } else {
            return m;
        }
    }


    public byte[] readUntil(byte... separators) {
        for (long cur = readerIndex; cur <= size - separators.length; cur++) {
            if (NativeUtil.matches(segment, cur, separators)) {
                byte[] result = cur == readerIndex ?
                        Constants.EMPTY_BYTES :
                        segment.asSlice(readerIndex, cur-readerIndex).toArray(ValueLayout.JAVA_BYTE);
                readerIndex = cur + separators.length;
                return result;
            }
        }

        return null;
    }

    public String readCStr() {
        byte[] bytes = readUntil(Constants.NUT);


        if (bytes == null || bytes.length == 0) {
            return null;
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }
}
