package com.fnmain.net.reactor.rwThread;

public enum PollerTaskType {
    BIND,  //Bind: 绑定事件用于连接建立之后, 将建立的channel结构绑定
    UNBIND, //unbind:强制取消连接
    CLOSE, //close, 强制中断连接
}




