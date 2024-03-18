package com.fnmain.net;

import com.fnmain.exception.ExceptionType;
import com.fnmain.exception.FrameworkException;
import com.fnmain.net.config.*;
import com.fnmain.net.enums.Constants;
import com.fnmain.net.enums.State;
import com.fnmain.net.platform.OsNetworkLibrary;
import com.fnmain.net.reactor.control.AbstractLifeCycle;
import com.fnmain.net.reactor.control.DurationCallBack;
import com.fnmain.net.reactor.control.Mutex;
import com.fnmain.net.reactor.control.SentryWithCallback;
import com.fnmain.net.reactor.process.Provider;
import com.fnmain.net.reactor.process.Sentry;
import com.fnmain.net.reactor.resource.*;
import com.fnmain.net.reactor.rwThread.*;
import com.fnmain.net.reactor.rwThread.threadType.ListenerTask;
import com.fnmain.net.reactor.rwThread.threadType.PollerTask;
import org.jctools.queues.MpscLinkedQueue;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;



public class Net extends AbstractLifeCycle {
    private static final AtomicBoolean instanceFlag = new AtomicBoolean(false);
    private final State state = new State();

    private final static OsNetworkLibrary OS = OsNetworkLibrary.CURRENT;
    private final static Mux mux = OS.createMux();

    private final Queue<ListenerTask> netQueue = new MpscLinkedQueue<>();

    private static final AtomicInteger counter = new AtomicInteger(0);


    private static final NetConfig defaultNetConfig = new NetConfig();
    private static final PollerConfig defaultPollerConfig = new PollerConfig();
    private static final WriterConfig defaultWriteConfig = new WriterConfig();
    private static final SocketConfig defaultSocketConfig = new SocketConfig();


    private List<Poller> pollers;
    private List<Writer> writers;

    
    public Net(NetConfig netConfig) {
        if (!instanceFlag.compareAndSet(false, true)) {
            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        }
    }

    public Net(NetConfig netConfig, PollerConfig pollerConfig, WriterConfig writerConfig) {
        if (netConfig == null || pollerConfig == null || writerConfig == null) {
            throw new NullPointerException();
        }

        if (!instanceFlag.compareAndSet(false, true)) {
            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        }

        int pollerCount = pollerConfig.getPollerCount();

        if (pollerCount <= 0) {
            throw new FrameworkException(ExceptionType.NETWORK, "Poller instance cannot be zero");
        }

        int writerCount = writerConfig.getWriterCount();

        if (writerCount <= 0) {
            throw new FrameworkException(ExceptionType.NETWORK, "Writer instances cannot be zero");
        }

        this.pollers = IntStream.range(0, pollerCount).mapToObj(_ -> new Poller(pollerConfig)).toList();
        this.writers = IntStream.range(0, writerCount).mapToObj(_ -> new Writer(writerConfig)).toList();

    }

    public void addProvider(Provider provider) {

        try (Mutex _ = state.withMutex()) {

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getValueNotNull(T value) {
        return Objects.requireNonNull(value);
    }


    public void addServerListener(ListenerConfig listenerConfig) {
        try (Mutex _ = state.withMutex()) {

            int current = state.get();

            if (current > Constants.RUNNING) {
                throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
            }

            Supplier<Encoder> encoderSupplier = getValueNotNull(listenerConfig.getEncoderSupplier());
            Supplier<Decoder> decoderSupplier = getValueNotNull(listenerConfig.getDecoderSupplier());
            Supplier<Handler> handlerSupplier = getValueNotNull(listenerConfig.getHandlerSupplier());

            Provider provider = getValueNotNull(listenerConfig.getProvider());

            Loc loc = getValueNotNull(listenerConfig.getLoc());

            SocketConfig socketConfig = getValueNotNull(listenerConfig.getSocketConfig());

            Socket socket = OS.createSocket(loc);

            OS.configureServerSocket(socket, loc, socketConfig);

            ListenerTask listenerTask = new ListenerTask(encoderSupplier, decoderSupplier, handlerSupplier, provider, loc, new AtomicInteger(0), socket, socketConfig);

            if (!netQueue.offer(listenerTask)) { //添加一个元素
                throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
            }

            OS.bindAndListen(socket, loc, defaultNetConfig.getBacklog());

            OS.ctlMux(mux, socket, Constants.NET_NONE, Constants.NET_R);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void connect(Loc loc, Encoder encoder, Decoder decoder, Handler handler, Provider provider, SocketConfig socketConfig, DurationCallBack durationCallBack) {
        Socket socket = OS.createSocket(loc);

        OS.configureClientSocket(socket, socketConfig);

        int seq = counter.getAndIncrement();

        Poller poller = pollers.get(seq % pollers.size());
        Writer writer = writers.get(seq % writers.size());

        Channel channel = new ChannelImpl(socket, encoder, decoder, handler, poller, writer, loc);

        addProvider(provider);

        Sentry sentry = provider.create(channel);

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment addr = OS.createSockAddr(loc, arena);

            int r = OS.connect(socket, addr);

            if (r == 0) {

                PollerTask pollerTask = new PollerTask(PollerTaskType.BIND, channel, sentry);
                poller.submit(pollerTask);

                OS.ctlMux(poller.mux(), socket, Constants.NET_NONE, Constants.NET_W);
            } else if (r < 0) {
                int errno = Math.abs(r);

                if (errno == OS.connectBlockCode()) {
                    Duration duration = durationCallBack.duration();
                    Runnable callBack = durationCallBack.callBack();

                    poller.submit(new PollerTask(PollerTaskType.BIND, channel, callBack==null? sentry:new SentryWithCallback(sentry, callBack)));
                    //TODO: 这里还需要添加时间轮的逻辑
                } else {
                    throw new FrameworkException(ExceptionType.NATIVE, STR."Failed to connect, error:\{errno}");
                }
            } else {

            }
        }
    }



    @Override
    protected void doInit() {

    }

    @Override
    protected void doExit() throws InterruptedException {

    }
}
