package com.sfmand.des.engine;

/** A system under simulation: state, statistics and event types. */
public interface Model {

    String name();

    void initialize(Simulation sim);

    void updateStatistics(double dt);

    String stateSnapshot();

    long completions();

    Report report(double finalClock);
}
