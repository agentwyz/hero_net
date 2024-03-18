package com.fnmain.utils;

import com.fnmain.exception.ExceptionType;
import com.fnmain.exception.FrameworkException;
import com.fnmain.net.enums.Constants;
import com.fnmain.net.enums.OsType;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;

public class NativeUtil {


    private static final String osName = System.getProperty("os.name").toLowerCase();
    private static final OsType osType = detectOsType();

    public static final int CPU_CORES = Runtime.getRuntime().availableProcessors();
    public static final MemorySegment NULL_POINTER = MemorySegment.ofAddress(0L);


    private static final VarHandle intHandle = MethodHandles.memorySegmentViewVarHandle(ValueLayout.JAVA_INT);
    private static final VarHandle byteHandle = MethodHandles.memorySegmentViewVarHandle(ValueLayout.JAVA_BYTE);
    private static final VarHandle shortHandle = MethodHandles.memorySegmentViewVarHandle(ValueLayout.JAVA_SHORT);
    private static final VarHandle longHandle = MethodHandles.memorySegmentViewVarHandle(ValueLayout.JAVA_LONG);


    private static OsType detectOsType() {
        if (osName.contains("windows")) {
            return OsType.Windows;
        } else if (osName.contains("linux")) {
            return OsType.Linux;
        } else if (osName.contains("mac") && osName.contains("os")) {
            return OsType.MacOS;
        } else {
            return OsType.Unknown;
        }
    }


    public static String getDynamicLibraryName(String identifier) {
        return switch (osType) {
            case Windows -> STR."lib\{identifier}.dll";
            case Linux -> STR."lib\{identifier}.so";
            case MacOS -> STR."lib\{identifier}.dylib";
            default -> throw new FrameworkException(ExceptionType.NATIVE, "Unrecognized operating system");
        };
    }

    

    public static OsType osType() {
        return osType;
    }

    private NativeUtil() {
        throw new UnsupportedOperationException();
    }




    public static boolean checkNullPointer(MemorySegment memorySegment) {
        return memorySegment == null || memorySegment.address() == 0L;
    }

    public static int getInt(MemorySegment memorySegment, long index) {
        return (int) intHandle.get(memorySegment, index);
    }

    public static void setInt(MemorySegment memorySegment, long index, int value) {
        intHandle.set(memorySegment, index, value);
    }

    public static byte getByte(MemorySegment memorySegment, long index) {
        return (byte) byteHandle.get(memorySegment, index);
    }

    public static void setByte(MemorySegment memorySegment, long index, byte value) {
        byteHandle.set(memorySegment, index, value);
    }

    public static short getShort(MemorySegment memorySegment, long index) {
        return (short) shortHandle.get(memorySegment, index);
    }

    public static void setShort(MemorySegment memorySegment, long index, short value) {
        byteHandle.set(memorySegment, index, value);
    }

    public static long getLong(MemorySegment memorySegment, long index) {
        return (long) longHandle.get(memorySegment, index);
    }

    public static void setLong(MemorySegment memorySegment, long index, long value) {
        longHandle.set(memorySegment, index, value);
    }

    public static String getStr(MemorySegment memorySegment) {
        return getStr(memorySegment, 0);
    }

    public static String getStr(MemorySegment memorySegment, int maxLength) {
        if (maxLength > 0) {
            byte[] bytes = new byte[maxLength];
            return getString(memorySegment, maxLength, bytes);
        } else {
            return getString(memorySegment);
        }


    }

    private static String getString(MemorySegment memorySegment) {
        for (int i = 0; i < Integer.MAX_VALUE; i++) {

            byte b = getByte(memorySegment, i);

            if (b == Constants.NUT) {
                byte[] bytes = new byte[i];
                MemorySegment.copy(memorySegment, ValueLayout.JAVA_BYTE, 0, bytes, 0, i);
                return new String(bytes, StandardCharsets.UTF_8);
            }
        }

        throw new FrameworkException(ExceptionType.NATIVE, Constants.UNREACHED);
    }


    private static String getString(MemorySegment memorySegment, int maxLength, byte[] bytes) {
        for (int i = 0; i < maxLength; i++) {

            byte b = getByte(memorySegment, i);

            //如果已经结尾了, 直接返回, 如果没有结尾继续赋值
            if (b == Constants.NUT)
                return new String(bytes, 0, i, StandardCharsets.UTF_8);
            else
                bytes[i] = b;
        }

        throw new FrameworkException(ExceptionType.NATIVE, Constants.UNREACHED);
    }

    public static MemorySegment allocateStr(Arena arena, String str) {
        return arena.allocateUtf8String(str);
    }

    public static MemorySegment allocateStr(Arena arena, String str, int len) {
        MemorySegment strSegment = MemorySegment.ofArray(str.getBytes(StandardCharsets.UTF_8));

        long size = strSegment.byteSize();

        if (len < size + 1) {
            throw new RuntimeException("string out of range");
        }

        MemorySegment memorySegment = arena.allocateArray(ValueLayout.JAVA_BYTE, len);
        MemorySegment.copy(strSegment, 0, memorySegment, 0, size);
        setByte(memorySegment, size, Constants.NUT);

        return memorySegment;
    }

    public static boolean matches(MemorySegment m, long offset, byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if (NativeUtil.getByte(m, offset + i) != bytes[i]) {
                return false;
            }
        }

        return true;
    }

    public static int getCpuCores() {
        return CPU_CORES;
    }


    public static int castInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new FrameworkException(ExceptionType.NATIVE, Constants.UNREACHED);
        }

        return (int) l;
    }
}
