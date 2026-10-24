package com.sfmand.des.engine;

import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractEvent implements Event {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    private final double time;
    private final long sequence = SEQUENCE.getAndIncrement();

    protected AbstractEvent(double time) {
        if (Double.isNaN(time) || time < 0) {
            throw new IllegalArgumentException("Event time must be a non-negative number: " + time);
        }
        this.time = time;
    }

    @Override
    public final double time() {
        return time;
    }

    @Override
    public final long sequence() {
        return sequence;
    }

    @Override
    public String toString() {
        return "(" + type() + ", " + Numbers.format(time) + ")";
    }
}
