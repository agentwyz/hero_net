package com.fnmain.net.config;

public final class SocketConfig {
    private boolean reuseAddr = true;
    private boolean keepAlive = false;
    private boolean tcpNoDelay = true;
    private boolean ipv6only = false;

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public boolean isReuseAddr() {
        return reuseAddr;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public boolean isIpv6only() {
        return ipv6only;
    }

    public void setReuseAddr(boolean reuseAddr) {
        this.reuseAddr = reuseAddr;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public void setIpv6only(boolean ipv6only) {
        this.ipv6only = ipv6only;
    }
}
