package com.fnmain.net.config;

import com.fnmain.net.enums.Constants;
import com.fnmain.utils.NativeUtil;


public class PollerConfig {
    private int pollerCount = Math.max(NativeUtil.getCpuCores() >> 1, 4);

    private int maxEvents = 16;

    private int muxTimeout = 25;

    private int readBufferSize = 64 * Constants.KB;

    private int mapSize = Constants.KB;


    public int getPollerCount() {
        return this.pollerCount;
    }


    public int getMaxEvents() {
        return maxEvents;
    }

    public void setPollerCount(int pollerCount) {
        this.pollerCount = pollerCount;
    }

    public void setMaxEvents(int maxEvents) {
        this.maxEvents = maxEvents;
    }

    public int getMuxTimeout() {
        return muxTimeout;
    }

    public void setMuxTimeout(int muxTimeout) {
        this.muxTimeout = muxTimeout;
    }

    public int getReadBufferSize() {
        return readBufferSize;
    }

    public void setReadBufferSize(int readBufferSize) {
        this.readBufferSize = readBufferSize;
    }

    public int getMapSize() {
        return mapSize;
    }

    public void setMapSize(int mapSize) {
        this.mapSize = mapSize;
    }
}
