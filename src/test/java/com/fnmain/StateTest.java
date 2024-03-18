package com.fnmain;

import com.fnmain.net.enums.State;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StateTest {

    @Test
    public void should_return_registered_value() {
        //0000 0001
        //左移2位, 0000 0100
        State state = new State(1);

        state.register(2);

        assertEquals(new Integer[]{4}, state.get());

    }
}
