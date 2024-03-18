package com.fnmain.net.config;

import com.fnmain.net.reactor.resource.Decoder;
import com.fnmain.net.reactor.resource.Encoder;
import com.fnmain.net.reactor.resource.Handler;
import com.fnmain.net.reactor.process.Provider;
import com.fnmain.net.reactor.resource.Loc;

import java.util.function.Supplier;

public final class ListenerConfig {

    private Loc loc;

    private Supplier<Encoder> encoderSupplier;
    private Supplier<Decoder> decoderSupplier;
    private Supplier<Handler> handlerSupplier;

    private SocketConfig socketConfig = new SocketConfig();

    private Provider provider;

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Provider getProvider() {
        return provider;
    }

    public Loc getLoc() {
        return loc;
    }

    public void setLoc(Loc loc) {
        this.loc = loc;
    }

    public Supplier<Encoder> getEncoderSupplier() {
        return encoderSupplier;
    }

    public void setEncoderSupplier(Supplier<Encoder> encoderSupplier) {
        this.encoderSupplier = encoderSupplier;
    }

    public Supplier<Decoder> getDecoderSupplier() {
        return decoderSupplier;
    }

    public void setDecoderSupplier(Supplier<Decoder> decoderSupplier) {
        this.decoderSupplier = decoderSupplier;
    }

    public Supplier<Handler> getHandlerSupplier() {
        return handlerSupplier;
    }

    public void setHandlerSupplier(Supplier<Handler> handlerSupplier) {
        this.handlerSupplier = handlerSupplier;
    }

    public SocketConfig getSocketConfig() {
        return socketConfig;
    }

    public void setSocketConfig(SocketConfig socketConfig) {
        this.socketConfig = socketConfig;
    }
}