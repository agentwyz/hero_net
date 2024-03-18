package com.fnmain.net.enums;

import com.fnmain.net.reactor.control.Mutex;

/*
在我们网络编程中, 我们会用int变量去表示这些状态
但是这个变量可能在多线程环境下面, 所以我们需要对它进行上锁
 */



public final class State {
    private final Mutex mutex = new Mutex();
    private int state;

    public State() {
        this(0);
    }

    public State(int initialState) {
        this.state = initialState;
    }

    public Mutex withMutex() {
        return mutex.acquire();
    }

    public int get() {
        return state;
    }

    public void set(int state) {
        this.state = state;
    }


    //表示左移两位
    public void register(int mask) {
        this.state |= mask;
    }

    public boolean unregister(int mask) {
        boolean r = (state & mask) > 0;
        state &= ~mask;
        return r;
    }

    public boolean cas(int expectedValue, int newValue) {
        if (state == expectedValue) {
            state = newValue;
            return true;
        } else {
          return false;
        }
    }


}
