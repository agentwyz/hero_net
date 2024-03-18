package com.fnmain.net.platform;

import com.fnmain.exception.ExceptionType;
import com.fnmain.exception.FrameworkException;
import com.fnmain.net.reactor.rwThread.Mux;
import com.fnmain.net.enums.Constants;
import com.fnmain.net.reactor.resource.Socket;
import com.fnmain.net.reactor.control.Timeout;
import com.fnmain.utils.NativeUtil;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.UnionLayout;
import java.lang.foreign.ValueLayout;

public final class LinuxNetworkLibrary implements OsNetworkLibrary {

    private static final UnionLayout epollDataLayout = MemoryLayout.unionLayout(
            ValueLayout.ADDRESS_UNALIGNED.withName("ptr"),
            ValueLayout.JAVA_INT_UNALIGNED.withName("fd"),
            ValueLayout.JAVA_INT_UNALIGNED.withName("u32"),
            ValueLayout.JAVA_LONG_UNALIGNED.withName("u64")
    );

    private static final MemoryLayout epollEventLayout = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT_UNALIGNED.withName("events"),
            epollDataLayout.withName("data"));

    private static final long eventSize = epollEventLayout.byteSize();

    private static final long eventsOffset = epollDataLayout.byteOffset(MemoryLayout.PathElement.groupElement("events"));

    private static final long dataOffset = epollDataLayout.byteOffset(MemoryLayout.PathElement.groupElement("data"));

    private static final long fdOffset = epollDataLayout.byteOffset(MemoryLayout.PathElement.groupElement("fd"));

    private final int connectBlockCode;
    private final int sendBlockCode;


    public LinuxNetworkLibrary() {

    }




    @Override
    public Mux createMux() {
        return null;
    }

    @Override
    public MemoryLayout eventLayout() {
        return epollEventLayout;
    }

    @Override
    public int muxWait(Mux mux, MemoryLayout events, int maxEvents, Timeout timeout) {

        return 0;
    }

    @Override
    public int setIpv4SockAddr(MemorySegment sockAddr, MemorySegment address, short port) {
        return 0;
    }

    @Override
    public int setIpv6SockAddr(MemorySegment sockAddr, MemorySegment address, short port) {
        return 0;
    }

    @Override
    public int setNonBlocking(Socket socket) {
        return 0;
    }

    @Override
    public short getIpv4Port(MemorySegment addr) {
        return 0;
    }

    @Override
    public short getIpv6Port(MemorySegment addr) {
        return 0;
    }

    @Override
    public Socket createIpv4Socket() {
        return null;
    }

    @Override
    public Socket createIpv6Socket() {
        return null;
    }

    @Override
    public int setTcpNoDelay(Socket socket, boolean b) {
        return 0;
    }

    @Override
    public int setIpv6Only(Socket socket, boolean b) {
        return 0;
    }

    @Override
    public IntPair access(MemorySegment events, int index) {
        int event = NativeUtil.getInt(events, index * eventSize + eventsOffset);
        int socket = NativeUtil.getInt(events, index * eventSize + dataOffset + fdOffset);

        if ((event & (Constants.EPOLL_IN | Constants.EPOLL_RDHUP)) != 0) {
            return new IntPair(socket, Constants.NET_R);
        } else {
            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        }
    }

    /*
    ctl函数将用于修改多路复用事件的注册状态
     */


    @Override
    public int ctl(Mux mux, Socket socket, int from, int to) {
        if (from == to) {
            return 0;
        }

        int epfd = mux.epfd();
        int fd = socket.intValue();

        if (to == Constants.NET_NONE) {

        }
    }

    @Override
    public int muxWait(Mux mux, MemoryLayout events, int maxEvent, Timeout timeout) {
        return 0;
    }

    @Override
    public int bind(Socket socket, MemoryLayout addr) {
        return 0;
    }

    @Override
    public int listen(Socket socket, int backlog) {
        return 0;
    }

    @Override
    public int bind(Socket socket, MemorySegment addr) {
        return 0;
    }

    @Override
    public int listen(Socket socket, MemorySegment sockAddr) {
        return 0;
    }

    @Override
    public int connect(Socket socket, MemorySegment sockAddr) {
        return 0;
    }

    @Override
    public Socket accept(Socket socket, MemorySegment addr) {
        return null;
    }

    @Override
    public int recv(Socket socket, MemorySegment data, int len) {
        return 0;
    }

    @Override
    public int send(Socket socket, MemorySegment data, int len) {
        return 0;
    }

    @Override
    public int getIpv4Address(MemorySegment clientAddr, MemorySegment address) {
        return 0;
    }

    @Override
    public int getIpv6Address(MemorySegment clientAddr, MemorySegment address) {
        return 0;
    }

    @Override
    public int getErrOpt(Socket socket) {
        return 0;
    }

    @Override
    public int shutdownWrite(Socket socket) {
        return 0;
    }

    @Override
    public int closeSocket(Socket socket) {
        return 0;
    }

    @Override
    public void exit() {

    }

    @Override
    public int connectBlockCode() {
        return connectBlockCode;
    }

    @Override
    public int sendBlockCode() {
        return 0;
    }

    @Override
    public int interruptCode() {
        return 0;
    }

    @Override
    public int ipv4AddressLen() {
        return 0;
    }

    @Override
    public int ipv4AddressSize() {
        return 0;
    }

    @Override
    public int ipv4AddressAlign() {
        return 0;
    }

    @Override
    public int ipv6AddressLen() {
        return 0;
    }

    @Override
    public int ipv6AddressSize() {
        return 0;
    }

    @Override
    public int ipv6AddressAlign() {
        return 0;
    }

    @Override
    public int setKeepAlive(Socket socket, boolean bool) {
        return 0;
    }

    @Override
    public int setReuseAddr(Socket socket, boolean b) {
        return 0;
    }
}
