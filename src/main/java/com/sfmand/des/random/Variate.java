package com.sfmand.des.random;

public interface Variate {

    double next();

    default boolean hasNext() {
        return true;
    }
}
