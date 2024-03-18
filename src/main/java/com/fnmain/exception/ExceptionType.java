package com.fnmain.exception;

public enum ExceptionType {
    CONTEXT, //Context, 表示上下文中产生的异常
    NATIVE, //native表示从Java中调用C语言动态库时候出现的错误
    NETWORK, //表示网络框架处理读写请求时候产生的异常
}
