package com.fnmain.net.reactor.resource;


/*
这个接口相关的操作, 通常是
 */

public interface Handler {
    //表示连接
    void onConnected(Channel channel);

    //表示accept
    void onRecv(Channel channel, Object data);

    void onShutdown(Channel channel);

    void onRemoved(Channel channel);
}
