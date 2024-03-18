package com.fnmain.net.reactor.control;



import com.fnmain.net.enums.Constants;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractLifeCycle implements LifeCycle {
    private final AtomicInteger state = new AtomicInteger(Constants.INITIAL);

    protected abstract void doInit();

    protected abstract void doExit() throws InterruptedException;


    @Override
    public void init() {
        if (state.compareAndSet(Constants.INITIAL, Constants.RUNNING)) {
            doInit();
        } else {
            throw new RuntimeException(Constants.UNREACHED);
        }
    }

    @Override
    public void exit() throws InterruptedException {
        if (state.compareAndSet(Constants.INITIAL, Constants.STOPPED)) {
            doExit();
        } else {
            throw new RuntimeException(Constants.UNREACHED);
        }
    }
}
