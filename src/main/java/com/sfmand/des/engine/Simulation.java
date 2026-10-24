package com.sfmand.des.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Simulation {

    /** When true, every loop iteration prints a state-trace row to the console. */
    public static boolean TRACE_MODE = false;

    private final Model model;
    private final TerminationCondition termination;
    private final FutureEventList fel = new FutureEventList();
    private final List<TraceRow> trace = new ArrayList<>();

    private double clock;
    private long eventCount;
    private boolean stopped;
    private boolean started;

    public Simulation(Model model, TerminationCondition termination) {
        this.model = model;
        this.termination = termination;
    }

    public Report run() {
        if (started) {
            throw new IllegalStateException("A Simulation instance can only run once");
        }
        started = true;

        // Initialization: clock = 0, empty FEL, model schedules its initial events
        clock = 0.0;
        model.initialize(this);
        if (TRACE_MODE) {
            System.out.println("=== Trace: " + model.name() + " ===");
            System.out.println(TraceRow.HEADER);
        }

        while (!stopped && !fel.isEmpty() && !termination.isMet(this)) {
            Event event = fel.poll();

            double dt = event.time() - clock;
            clock = event.time();
            model.updateStatistics(dt);
            event.execute(this);
            eventCount++;

            if (TRACE_MODE) {
                TraceRow row = TraceRow.capture(clock, event, model.stateSnapshot(), fel);
                trace.add(row);
                System.out.println(row);
            }
        }

        return model.report(clock);
    }

    public void schedule(Event event) {
        if (event.time() < clock) {
            throw new IllegalStateException("Cannot schedule " + event + " in the past (clock="
                    + Numbers.format(clock) + ")");
        }
        fel.schedule(event);
    }

    public void stop() {
        stopped = true;
    }

    public double clock() {
        return clock;
    }

    public long eventCount() {
        return eventCount;
    }

    public Model model() {
        return model;
    }

    public FutureEventList fel() {
        return fel;
    }

    public List<TraceRow> trace() {
        return Collections.unmodifiableList(trace);
    }
}
