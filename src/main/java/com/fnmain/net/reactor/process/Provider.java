package com.fnmain.net.reactor.process;

import com.fnmain.net.reactor.resource.Channel;

/*
提供这个Provider接口就是给我们开发人员留出一个自定义的资源释放的切入点

因为Sentry和Protocol有可能依赖一些Native层实现的组件, 这些组件可能在生命周期
终结的时候, 需要手动的进行资源释放的操作, 这个时候可以调用close来完成这些操作
*/



public interface Provider {
    Sentry create(Channel channel);
    default void close() {

    }
}
