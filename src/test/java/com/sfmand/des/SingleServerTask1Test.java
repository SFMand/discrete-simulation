package com.sfmand.des;

import com.sfmand.des.engine.EndOfSimulationEvent;
import com.sfmand.des.engine.Report;
import com.sfmand.des.engine.Simulation;
import com.sfmand.des.engine.TerminationCondition;
import com.sfmand.des.engine.TraceRow;
import com.sfmand.des.model.singleserver.SingleServerModel;
import com.sfmand.des.random.DeterministicSequence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** inter-arrival [8, 6, 1, 8], service [4, 1, 4, 3]. */
class SingleServerTask1Test {

    private static final double EPS = 1e-9;

    @BeforeEach
    void traceOn() {
        Simulation.TRACE_MODE = true;
    }

    @AfterEach
    void traceOff() {
        Simulation.TRACE_MODE = false;
    }

    private static SingleServerModel task1Model() {
        return new SingleServerModel(
                new DeterministicSequence(8, 6, 1, 8),
                new DeterministicSequence(4, 1, 4, 3),
                4);
    }

    @Test
    void firstFourEventsMatchExpectedTrace() {
        Simulation sim = new Simulation(task1Model(), TerminationCondition.maxEvents(4));
        Report report = sim.run();

        List<TraceRow> trace = sim.trace();
        assertEquals(4, trace.size());
        assertRow(trace.get(0), 0, "Arrival", "n=1, status=Busy", "[(D, 4), (A, 8)]");
        assertRow(trace.get(1), 4, "Departure", "n=0, status=Idle", "[(A, 8)]");
        assertRow(trace.get(2), 8, "Arrival", "n=1, status=Busy", "[(D, 9), (A, 14)]");
        assertRow(trace.get(3), 9, "Departure", "n=0, status=Idle", "[(A, 14)]");

        assertEquals(9, report.get("clock"), EPS);
        assertEquals(2, report.get("completions"), EPS);
        assertEquals(0, report.get("w"), EPS);
        assertEquals(2.5, report.get("W"), EPS);
        assertEquals(5.0 / 9, report.get("n_bar"), EPS);
        assertEquals(5.0 / 9, report.get("U"), EPS);
    }

    @Test
    void fullRunOfFourCustomers() {
        // Arrivals at 0, 8, 14, 15. Customer 4 waits 15 -> 18, departs at 21.
        Simulation sim = new Simulation(task1Model(), TerminationCondition.never());
        Report report = sim.run();

        assertEquals(8, sim.eventCount());
        assertRow(sim.trace().get(5), 15, "Arrival", "n=2, status=Busy, q=1", "[(D, 18)]");
        assertEquals(21, report.get("clock"), EPS);
        assertEquals(4, report.get("completions"), EPS);
        assertEquals(3, report.get("sum_wq"), EPS);
        assertEquals(0.75, report.get("w"), EPS);
        assertEquals(3.75, report.get("W"), EPS);
        assertEquals(15, report.get("sa"), EPS);
        assertEquals(15.0 / 21, report.get("n_bar"), EPS);
        assertEquals(12, report.get("busy"), EPS);
        assertEquals(12.0 / 21, report.get("U"), EPS);
    }

    @Test
    void stopsWhenFourthCustomerStartsService() {
        // Customer 3 departs at 18 and customer 4 starts service right away.
        SingleServerModel model = task1Model();
        Simulation sim = new Simulation(model, model.untilServiceStarts(4));
        Report report = sim.run();

        assertEquals(18, report.get("clock"), EPS);
        assertEquals(4, report.get("nw"), EPS);
        assertEquals(3, report.get("completions"), EPS);
        assertEquals(3, report.get("sum_wq"), EPS);
        assertEquals(0.75, report.get("w"), EPS);
        assertEquals(12, report.get("sa"), EPS);
        assertEquals(9, report.get("busy"), EPS);
    }

    @Test
    void endOfSimulationEventStopsAtFixedTime() {
        SingleServerModel model = new SingleServerModel(
                new DeterministicSequence(8, 6, 1, 8),
                new DeterministicSequence(4, 1, 4, 3),
                4) {
            @Override
            public void initialize(Simulation sim) {
                super.initialize(sim);
                sim.schedule(new EndOfSimulationEvent(10));
            }
        };
        Report report = new Simulation(model, TerminationCondition.never()).run();

        // Busy over [0, 4] and [8, 9]; idle afterwards until the end at 10.
        assertEquals(10, report.get("clock"), EPS);
        assertEquals(0.5, report.get("U"), EPS);
        assertEquals(0.5, report.get("n_bar"), EPS);
    }

    private static void assertRow(TraceRow row, double clock, String type, String statePrefix, String fel) {
        assertEquals(clock, row.clock(), EPS);
        assertEquals(type, row.eventType());
        assertEquals(true, row.state().startsWith(statePrefix), () -> "state was " + row.state());
        assertEquals(fel, row.fel());
    }
}
