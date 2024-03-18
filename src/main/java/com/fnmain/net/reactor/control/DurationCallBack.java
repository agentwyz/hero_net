package com.fnmain.net.reactor.control;

import java.time.Duration;

public record DurationCallBack(Duration duration, Runnable callBack) {

}
