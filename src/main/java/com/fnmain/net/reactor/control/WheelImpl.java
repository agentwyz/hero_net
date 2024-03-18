package com.fnmain.net.reactor.control;

import java.util.concurrent.atomic.AtomicBoolean;

public class WheelImpl extends AbstractLifeCycle implements Wheel {

    private static final long ONE_TIME_MISSION = -1;
    private static final long CANCEL_ONE_TIME_MISSION = -2;

    private static final AtomicBoolean instanceFlag = new AtomicBoolean(false);

    private static final Wheel INSTANCE = new WheelImpl(Wheel.slots, Wheel.tick);

    public WheelImpl(int slots, long tick) {
        if (!instanceFlag) {

        }
    }


    @Override
    protected void doInit() {
        
    }

    @Override
    protected void doExit() throws InterruptedException {

    }
}
