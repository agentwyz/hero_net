package com.fnmain.net.reactor.resource;

import com.fnmain.exception.ExceptionType;
import com.fnmain.exception.FrameworkException;

/*
这个类相当于linux操作系统的SocketAdr
 */

public record Loc(IpType ipType, String ip, int port) {
    private static final int PORT_MAX = 65535;

    public short shortPort() {
        if (port < 0 || port > PORT_MAX) {
            throw new FrameworkException(ExceptionType.NETWORK, "Port number overflow");
        }

        return (short) port;
    }

    @Override
    public String toString() {
        return STR."[\{ip==null || ip.isBlank() ? "localhost" : ip}:\{port}]";
    }
}
