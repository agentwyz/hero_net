package com.fnmain.net.reactor.control;

public interface LifeCycle {
    void init();
    void exit() throws InterruptedException;
}
