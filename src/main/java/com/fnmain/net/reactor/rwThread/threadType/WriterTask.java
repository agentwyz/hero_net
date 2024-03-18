package com.fnmain.net.reactor.rwThread.threadType;

import com.fnmain.net.reactor.resource.Channel;
import com.fnmain.net.reactor.rwThread.WriterCallback;

public record WriterTask(WriterTaskType type, Channel channel, Object msg, WriterCallback writerCallback) {
}
