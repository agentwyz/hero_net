package com.fnmain.net.reactor.rwThread.threadType;


import com.fnmain.net.reactor.resource.Channel;
import com.fnmain.net.reactor.rwThread.PollerTaskType;

public record PollerTask(PollerTaskType type, Channel channel, Object msg) {

}
