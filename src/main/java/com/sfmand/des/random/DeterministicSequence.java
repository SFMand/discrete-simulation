package com.sfmand.des.random;

import java.util.NoSuchElementException;

public class DeterministicSequence implements Variate {

    private final double[] values;
    private int index;

    public DeterministicSequence(double... values) {
        this.values = values.clone();
    }

    @Override
    public double next() {
        if (!hasNext()) {
            throw new NoSuchElementException("Deterministic sequence exhausted after " + values.length + " values");
        }
        return values[index++];
    }

    @Override
    public boolean hasNext() {
        return index < values.length;
    }
}
