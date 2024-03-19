package com.fnmain.net.reactor.control;

import com.fnmain.exception.ExceptionType;
import com.fnmain.exception.FrameworkException;
import com.fnmain.net.enums.Constants;
import com.fnmain.net.reactor.process.Sentry;


public record SentryWithCallback(Sentry sentry, Runnable runnable) {
    public SentryWithCallback{
        if (sentry == null) {
            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        }
    }

}
