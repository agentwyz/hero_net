package com.fnmain.net.platform;

import com.fnmain.exception.ExceptionType;
import com.fnmain.exception.FrameworkException;
import com.fnmain.net.reactor.rwThread.Mux;
import com.fnmain.net.config.SocketConfig;
import com.fnmain.net.enums.Constants;
import com.fnmain.net.reactor.control.Timeout;
import com.fnmain.net.reactor.resource.IpType;
import com.fnmain.net.reactor.resource.Loc;
import com.fnmain.net.reactor.resource.Socket;
import com.fnmain.net.reactor.resource.SocketAndLoc;
import com.fnmain.utils.NativeUtil;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public interface OsNetworkLibrary {

    int connectBlockCode();

    int sendBlockCode();

    int interruptCode();

    int ipv4AddressLen();

    int ipv4AddressSize();

    int ipv4AddressAlign();

    int ipv6AddressLen();

    int ipv6AddressSize();

    int ipv6AddressAlign();

    int setKeepAlive(Socket socket, boolean bool);
    int setReuseAddr(Socket socket, boolean b);


    Mux createMux();

    long createEpoll();

    MemoryLayout eventLayout();


    IntPair access(MemorySegment events, int index);

    int ctl(Mux mux, Socket socket, int from, int to);

    int muxWait(Mux mux, MemorySegment events, int maxEvent, Timeout timeout);


    int setIpv4SockAddr(MemorySegment sockAddr, MemorySegment address, short port);
    int setIpv6SockAddr(MemorySegment sockAddr, MemorySegment address, short port);

    int setNonBlocking(Socket socket);

    short getIpv4Port(MemorySegment addr);

    short getIpv6Port(MemorySegment addr);


    Socket createIpv4Socket();
    Socket createIpv6Socket();

    int setTcpNoDelay(Socket socket, boolean b);

    int setIpv6Only(Socket socket, boolean b);


    int listen(Socket socket, int backlog);

    int bind(Socket socket, MemorySegment addr);

    int listen(Socket socket, MemorySegment sockAddr);

    int connect(Socket socket, MemorySegment sockAddr);

    Socket accept(Socket socket, MemorySegment addr);

    int recv(Socket socket, MemorySegment data, int len);

    int send(Socket socket, MemorySegment data, int len);

    int getIpv4Address(MemorySegment clientAddr, MemorySegment address);
    int getIpv6Address(MemorySegment clientAddr, MemorySegment address);

    int getErrOpt(Socket socket);

    int shutdownWrite(Socket socket);

    int closeSocket(Socket socket);

    void exit();


    default long createSocket_(Loc loc) {
        return 8;
    }


    OsNetworkLibrary CURRENT = switch (NativeUtil.osType()) {
        case Linux -> new LinuxNetworkLibrary();
        default -> throw new FrameworkException(ExceptionType.NATIVE, "Unsupported operating system");
    };


    default int check(int value, String errMsg) {
        if (value < 0) {
            int errno = Math.abs(value);
            throw new FrameworkException(ExceptionType.NETWORK, STR."failed to \{errMsg} with error code:\{errno}");
        }

        return value;
    }


    default void ctlMux(Mux mux, Socket socket, int from, int to) {
        check(ctl(mux, socket, from, to), "ctl mux");
    }

    default void ctlMux_(long id, long socketId, int form, int to) {
        
    }

    default MemorySegment createSockAddr(Loc loc, Arena arena) {
        if (loc.ipType() == IpType.IPV4) {
            return createIpv4SockAddr(loc, arena);
        } else if (loc.ipType() == IpType.IPV6) {
            return createIpv6SockAddr(loc, arena);
        } else {
            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        }
    }


    default MemorySegment createIpv4SockAddr(Loc loc, Arena arena) {
        MemorySegment r = arena.allocate(ipv4AddressSize(), ipv4AddressAlign());

        MemorySegment ip = loc.ip() == null || loc.ip().isBlank() ?
                NativeUtil.NULL_POINTER : NativeUtil.allocateStr(arena, loc.ip(), ipv4AddressLen());

        int setId = check(setIpv4SockAddr(r, ip, loc.shortPort()), "set ipv4 address");

        if (setId == 0) {
            throw new FrameworkException(ExceptionType.NETWORK, STR."IPV4 address is not valid:\{loc.ip()}");
        }

        return r;
    }

    default MemorySegment createIpv6SockAddr(Loc loc, Arena arena) {
        MemorySegment r = arena.allocate(ipv6AddressSize(), ipv6AddressAlign());

        MemorySegment ip = loc.ip() == null | loc.ip().isBlank() ?
                NativeUtil.NULL_POINTER : NativeUtil.allocateStr(arena, loc.ip(), ipv6AddressLen());

        int setId = check(setIpv6SockAddr(r, ip, loc.shortPort()), "set ipv6 address");

        if (setId == 0) {
            throw new FrameworkException(ExceptionType.NATIVE, STR."IPV6 address is not valid:\{loc.ip()}");
        }

        return r;
    }



    default Socket createSocket(Loc loc) {
        return switch (loc.ipType()) {
            case IPV4 -> createIpv4Socket();
            case IPV6 -> createIpv6Socket();
            case null -> throw new FrameworkException(ExceptionType.NATIVE, Constants.UNREACHED);
        };
    }


    default void configureClientSocket(Socket socket, SocketConfig socketConfig) {
        check(setKeepAlive(socket, socketConfig.isKeepAlive()), "set client SO_REUSE_ADDR");
        check(setTcpNoDelay(socket, socketConfig.isKeepAlive()), "set server SO_KEEPALIVE");
        check(setNonBlocking(socket), "set client non-blocking");
    }

    default void configureServerSocket(Socket socket, Loc loc, SocketConfig socketConfig) {
        check(setReuseAddr(socket, socketConfig.isReuseAddr()), "set server so_reuse_addr");
        check(setKeepAlive(socket, socketConfig.isKeepAlive()), "set server SO_KEEPALIVE");
        check(setTcpNoDelay(socket, socketConfig.isTcpNoDelay()), "set server IPV6_V6ONLY");

        if (loc.ipType() == IpType.IPV6) {
            check(setIpv6Only(socket, socketConfig.isIpv6only()), "set server IPV6_V6ONLY");
        }
    }


    default void bindAndListen(Socket socket, Loc loc, int backlog) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment addr = createSockAddr(loc, arena);
            check(bind(socket, addr), "bind");
            check(listen(socket, backlog), "listen");
        }
    }

    default SocketAndLoc accept(Loc loc, Socket socket, SocketConfig socketConfig) {
        return switch (loc.ipType()) {
            case IPV4 -> acceptIpv4Connection(socket, socketConfig);
            case IPV6 -> acceptIpv6Connection(socket, socketConfig);
            case null -> throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        };
    }

    String IPV4_MAP_FORMAT = "::ffff:";
    int IPV4_PREFIX_LENGTH = IPV4_MAP_FORMAT.length();

    private  SocketAndLoc acceptIpv4Connection(Socket socket, SocketConfig socketConfig) {
        final int ipv4AddressLen = ipv4AddressLen();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment clientAddr = arena.allocate(ipv4AddressSize(), ipv4AddressAlign());

            MemorySegment address = arena.allocateArray(ValueLayout.JAVA_BYTE, ipv4AddressLen);

            Socket clientSocket = accept(socket, clientAddr);

            configureClientSocket(clientSocket, socketConfig);

            check(getIpv4Address(clientAddr, address), "get client's ipv4 address");

            String ip = NativeUtil.getStr(address, ipv4AddressLen);

            int port = 0xFFFF & getIpv4Port(clientAddr);

            Loc clientLoc = new Loc(IpType.IPV4, ip, port);

            return new SocketAndLoc(clientSocket, clientLoc);
        }
    }


    private SocketAndLoc acceptIpv6Connection(Socket socket, SocketConfig socketConfig) {
        final int ipv6AddressLen = ipv6AddressLen();

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment clientAddr = arena.allocate(ipv6AddressSize(), ipv6AddressAlign());
            MemorySegment address = arena.allocateArray(ValueLayout.JAVA_BYTE, ipv4AddressLen());

            Socket clientSocket = accept(socket, clientAddr);

            configureClientSocket(clientSocket, socketConfig);
            check(getIpv6Address(clientAddr, address), "get client's ipv6 address");
            String ip = NativeUtil.getStr(address, ipv6AddressLen);

            int port = 0xFFFF & getIpv6Port(clientAddr);

            if (ip.startsWith(IPV4_MAP_FORMAT)) {
                return new SocketAndLoc(clientSocket, new Loc(IpType.IPV4, ip.substring(IPV4_PREFIX_LENGTH), port));
            } else {
                return new SocketAndLoc(clientSocket, new Loc(IpType.IPV6, ip, port));
            }
        }
    }


    int muxWait_();
}



