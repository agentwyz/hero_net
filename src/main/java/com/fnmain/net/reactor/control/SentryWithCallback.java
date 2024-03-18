package com.fnmain.net.reactor.control;

import com.fnmain.exception.ExceptionType;
import com.fnmain.exception.FrameworkException;
import com.fnmain.net.enums.Constants;
import com.fnmain.net.reactor.process.Sentry;

/*
对于BID类型消息而言, 我们会判断当前PollerTask中携带的具体消息类型

 */
public record SentryWithCallback(Sentry sentry, Runnable runnable) {
    public SentryWithCallback{
        if (sentry == null) {
            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        }
    }

}
