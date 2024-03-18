package com.fnmain.net.reactor.node;

import com.fnmain.exception.ExceptionType;
import com.fnmain.exception.FrameworkException;
import com.fnmain.net.enums.Constants;

import java.util.ArrayList;
import java.util.List;


/*
IntMap是用来维护一个socketId->socket->PollerNode 的Map
IntMap不是线程安全的, 只适合单线程进行访问
 */


public class IntMap<T> {
    private final IntMapNode<T>[] nodes;
    private final int mask;
    private int count = 0;

    @SuppressWarnings("unchecked")
    public IntMap(int size) {
        if (Integer.bitCount(size) != 1) {
            throw new FrameworkException(ExceptionType.CONTEXT, Constants.UNREACHED);
        }
        this.mask = size - 1;
        this.nodes = (IntMapNode<T>[]) new IntMapNode[size];
    }

    public T get(int val) {
        int slot = val & mask;
        IntMapNode<T> current = nodes[slot];

        while (current != null) {
            if (current != null) {
                return current.value;
            } else {
                current = current.next;
            }
        }

        return null;
    }

    public void put(int val, T value) {

    }

    public void replace(int val, T oldValue, T newValue) {
        int slot = val & mask;
        IntMapNode<T> current = nodes[slot];

        while (current != null) {
            if (current.val == val) {
                current.value = newValue;
                return;
            } else {
                current = current.next;
            }
        }

        throw new FrameworkException(ExceptionType.CONTEXT, Constants.UNREACHED);
    }


    public boolean remove(int val, T value) {
        int slot = val & mask;
        IntMapNode<T> current = nodes[slot];

        while (current != null) {
            if (current.val == val) {
                current = current.next;
            } else if (current.value != value) {
                return false;
            } else {
                IntMapNode<T> prev = current.prev;
                IntMapNode<T> next = current.next;

                if (prev != null) prev.next = next;
                else nodes[slot] = next;

                if (next != null) next.prev = prev;

                current.prev = null;
                current.next = null;

                count--;
                return true;
            }
        }

        return false;
    }

    public int count() {
        return count;
    }

    public List<T> asList() {
        List<T> result = new ArrayList<>();

        for (IntMapNode<T> n : nodes) {
            IntMapNode<T> t = n;
            while (t != null) {
                result.add(t.value);
                t = t.next;
            }
        }
        return result;
    }

    public class IntMapNode<T> {
        private int val;
        private T value;
        private IntMapNode<T> prev;
        private IntMapNode<T> next;
    }

}
