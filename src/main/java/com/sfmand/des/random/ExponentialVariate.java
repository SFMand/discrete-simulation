package com.sfmand.des.random;

import java.util.Random;

/**
 * Exponential times by inverse transform: X = -mean * ln(U), U ~ Uniform(0, 1].
 */
public class ExponentialVariate implements Variate {

    private final double mean;
    private final Random random;

    public ExponentialVariate(double mean, long seed) {
        if (mean <= 0) {
            throw new IllegalArgumentException("Mean must be positive: " + mean);
        }
        this.mean = mean;
        this.random = new Random(seed);
    }

    @Override
    public double next() {
        return -mean * Math.log(1.0 - random.nextDouble());
    }
}
