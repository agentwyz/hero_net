package com.fnmain;

import com.fnmain.net.reactor.node.SentryPollerNode;
import com.fnmain.net.reactor.process.Sentry;
import org.junit.Test;

public class SentryPollerNodeTest {
    @Test
    public void should_() {
        Sentry sentry = new TcpSentry();
        SentryPollerNode node = new SentryPollerNode(sentry);


    }
}
