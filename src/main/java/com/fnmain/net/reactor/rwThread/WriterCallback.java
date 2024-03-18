package com.fnmain.net.reactor.rwThread;

import com.fnmain.net.reactor.resource.Channel;

public interface WriterCallback {
    void onSuccess(Channel channel); //这个用于成功的时候回调
    void onFailure(Channel channel);

    default void invokeOnSuccess(Channel channel) {
        try {
            onSuccess(channel);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    default void invokeOnFailure(Channel channel) {
        try {
            onFailure(channel);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}
