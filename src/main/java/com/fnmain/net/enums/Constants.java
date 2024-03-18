package com.fnmain.net.enums;

public class Constants {
    public static final byte[] EMPTY_BYTES = new byte[0];
    public static final String UNREACHED = "Shouldn't be reached";
    public static final byte NUT = (byte) '\0'; //表示字符串结尾函数


    public static final int INITIAL = 0;
    public static final int STARTING = 1;
    public static final int RUNNING = 2;
    public static final int CLOSING = 3;
    public static final int STOPPED = 4;


    public static final int NET_NONE = Integer.MIN_VALUE;
    public static final int NET_IGNORED = Integer.MIN_VALUE | 1;
    public static final int NET_UPDATE = Integer.MIN_VALUE | (1 << 2);
    public static final int NET_W = Integer.MIN_VALUE | (1 << 4); // register write only
    public static final int NET_PW = Integer.MIN_VALUE | (1 << 6); // register write if possible
    public static final int NET_R = Integer.MIN_VALUE | (1 << 8); // register read only
    public static final int NET_PR = Integer.MIN_VALUE | (1 << 10); // register read if possible
    public static final int NET_RW = NET_R | NET_W; // register read and write
    public static final int NET_OTHER = Integer.MIN_VALUE | (1 << 20);

    public static final int KB = 1024;



    /*----------这个地方对应的是epoll对应的事件类型------------------*/
    public static final int EPOLL_IN = 1;  //0001表示对应的文件描述符号可读
    public static final int EPOLL_OUT = 1 << 2; //0100, 表示
    public static final int EPOLL_ERR = 1 << 3;
    public static final int EPOLL_HUP = 1 << 4; //表示对应
    public static final int EPOLL_RDHUP = 1 << 13;

    /*----------这个地方对应的是epoll_ctl_注册的对应数据类型---------*/
    public static final int EPOLL_CTL_ADD = 1;
    public static final int EPOLL_CTL_DEL = 2;
    public static final int EPOLL_CTL_MOD = 3;


    public static final int NET_PC = Integer.MIN_VALUE | (1 << 12);
    public static final int NET_WC = Integer.MIN_VALUE | (1 << 16);
}
