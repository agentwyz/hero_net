package com.fnmain.net.reactor.rwThread.threadType;

import com.fnmain.net.config.SocketConfig;
import com.fnmain.net.reactor.resource.Decoder;
import com.fnmain.net.reactor.resource.Encoder;
import com.fnmain.net.reactor.resource.Handler;
import com.fnmain.net.reactor.process.Provider;
import com.fnmain.net.reactor.resource.Loc;
import com.fnmain.net.reactor.resource.Socket;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public record ListenerTask(
        Supplier<Encoder> encoderSupplier,
        Supplier<Decoder> decoderSupplier,
        Supplier<Handler> handlerSupplier,
        Provider provider,
        Loc loc,
        AtomicInteger counter,
        Socket socket,
        SocketConfig socketConfig)
{



}
