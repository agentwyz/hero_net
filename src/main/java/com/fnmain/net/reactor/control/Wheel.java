package com.fnmain.net.reactor.control;

/*
 *按照一定的时间查询下一个槽是否有任务需要执行
 *
 */


import java.time.Duration;

public sealed interface Wheel extends LifeCycle permits WheelImpl {

    int slots = Integer.getInteger("wheel.slots", 4096);  //表示时间轮所拥有的槽数
    long tick = Long.getLong("Wheel.tick", 10L); //表示时间轮槽位所消耗的毫秒时间


    static Wheel wheel() {
        return WheelImpl.INSTACNCE;
    }

    //通过这个方法来执行下次执行的任务
    Runnable addJob(Runnable mission, Duration delay);

    Runable addPeriodicJob();
}
