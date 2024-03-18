package com.fnmain.net.reactor.resource;

import com.fnmain.utils.NativeUtil;

/*
socket: 含有两个字段, 这个两个字段表示的都是一个含义, 那就创建Socket的号码
之所以会出现两个是因为, windows和linux对应的平台不同
 */


public record Socket(int intValue, long longValue) {
    public Socket(int socket) {
        this(socket, socket);
    }

    public Socket(long socket) {
        this(NativeUtil.castInt(socket), socket);
    }

    @Override
    public int hashCode() {
        return intValue;
    }
}
