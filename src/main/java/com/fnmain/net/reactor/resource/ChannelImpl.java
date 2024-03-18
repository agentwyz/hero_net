package com.fnmain.net.reactor.resource;


import com.fnmain.net.enums.Constants;
import com.fnmain.net.reactor.rwThread.threadType.WriterTaskType;
import com.fnmain.net.reactor.rwThread.threadType.WriterTask;
import com.fnmain.net.reactor.rwThread.Poller;
import com.fnmain.net.reactor.rwThread.Writer;
import com.fnmain.net.reactor.rwThread.WriterCallback;

import java.util.Collection;

public record ChannelImpl(Socket socket, Encoder encoder, Decoder decoder, Handler handler, Poller poller, Writer writer, Loc loc)
        implements Channel {

    @Override
    public void sendMsg(Object msg, WriterCallback writerCallback) {
        if (msg == null) {
            throw new RuntimeException(Constants.UNREACHED);
        }

        WriterTask writerTask = new WriterTask(WriterTaskType.SINGLE_MSG, this, msg, writerCallback);
        writer.submit(writerTask);
    }



    @Override
    public void sendMultipleMsg(Collection<Object> msgs, WriterCallback writerCallback) {
        if (msgs == null || msgs.isEmpty()) {
            throw new RuntimeException(Constants.UNREACHED);
        }

        WriterTask writerTask = new WriterTask(WriterTaskType.MULTIPLE_MSG, this, msgs, writerCallback);
        writer.submit(writerTask);
    }
}
