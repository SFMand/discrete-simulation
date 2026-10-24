package com.sfmand.des.engine;

public interface Event {

    /** Simulation time at which this event fires */
    double time();

    /** Short label used in the FEL trace */
    String type();

    /** Readable name used in the trace */
    String typeName();

    /** Tie-break for events at the same time, lower value executes first. */
    default int priority() {
        return 0;
    }

    /**
     * Creation order; final tie-break so equal events run first-come,
     * first-served.
     */
    long sequence();

    /** Event routine: update model state and schedule follow-up events. */
    void execute(Simulation sim);
}
