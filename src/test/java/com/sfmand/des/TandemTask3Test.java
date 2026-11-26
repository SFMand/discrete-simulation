package com.sfmand.des;

import com.sfmand.des.engine.Report;
import com.sfmand.des.engine.Simulation;
import com.sfmand.des.engine.TerminationCondition;
import com.sfmand.des.engine.TraceRow;
import com.sfmand.des.model.tandem.TandemModel;
import com.sfmand.des.random.DeterministicSequence;
import com.sfmand.des.random.ExponentialVariate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TandemTask3Test {

    private static final double EPS = 1e-9;

    @BeforeEach
    void traceOn() { Simulation.TRACE_MODE = true; }

    @AfterEach
    void traceOff() { Simulation.TRACE_MODE = false; }

    private static TandemModel twoStageModel() {
        return new TandemModel(
                new DeterministicSequence(2, 2),
                List.of(new DeterministicSequence(3, 3, 3), new DeterministicSequence(1, 4, 1)),
                3);
    }

    @Test
    void traceMatchesHandComputation() {
        Simulation sim = new Simulation(twoStageModel(), TerminationCondition.never());
        sim.run();
        List<TraceRow> t = sim.trace();

        assertEquals(9, t.size());
        assertRow(t.get(0), 0, "Arrival", "N=1 | S1:n=1,Busy | S2:n=0,Idle", "[(A, 2), (D1, 3)]");
        assertRow(t.get(1), 2, "Arrival", "N=2 | S1:n=2,Busy | S2:n=0,Idle", "[(D1, 3), (A, 4)]");
        assertRow(t.get(2), 3, "Departure 1", "N=2 | S1:n=1,Busy | S2:n=1,Busy", "[(D2, 4), (A, 4), (D1, 6)]");
        assertRow(t.get(3), 4, "Departure 2", "N=1 | S1:n=1,Busy | S2:n=0,Idle", "[(A, 4), (D1, 6)]");
        assertRow(t.get(4), 4, "Arrival", "N=2 | S1:n=2,Busy | S2:n=0,Idle", "[(D1, 6)]");
        assertRow(t.get(5), 6, "Departure 1", "N=2 | S1:n=1,Busy | S2:n=1,Busy", "[(D1, 9), (D2, 10)]");
        assertRow(t.get(6), 9, "Departure 1", "N=2 | S1:n=0,Idle | S2:n=2,Busy", "[(D2, 10)]");
        assertRow(t.get(7), 10, "Departure 2", "N=1 | S1:n=0,Idle | S2:n=1,Busy", "[(D2, 11)]");
        assertRow(t.get(8), 11, "Departure 2", "N=0 | S1:n=0,Idle | S2:n=0,Idle", "[]");
    }

    @Test
    void statisticsMatchHandComputation() {
        Report r = new Simulation(twoStageModel(), TerminationCondition.never()).run();
        assertEquals(11, r.get("clock"), EPS);
        assertEquals(3, r.get("completions"), EPS);
        assertEquals(1.0, r.get("w_1"), EPS);
        assertEquals(1.0 / 3, r.get("w_2"), EPS);
        assertEquals(9.0 / 11, r.get("U_1"), EPS);
        assertEquals(6.0 / 11, r.get("U_2"), EPS);
        assertEquals(19, r.get("sa"), EPS);
        assertEquals(19.0 / 3, r.get("W"), EPS);
        assertEquals(r.get("n_bar"), r.get("lambda_W"), EPS);
    }

    @Test
    void oneStageTandemEqualsTask1() {
        TandemModel model = new TandemModel(
                new DeterministicSequence(8, 6, 1, 8),
                List.of(new DeterministicSequence(4, 1, 4, 3)),
                4);
        Report r = new Simulation(model, TerminationCondition.never()).run();
        assertEquals(21, r.get("clock"), EPS);
        assertEquals(15.0 / 21, r.get("n_bar"), EPS);
        assertEquals(12.0 / 21, r.get("U_1"), EPS);
    }

    @Test
    void longRunSatisfiesLittlesLaw() {
        Simulation.TRACE_MODE = false;
        TandemModel model = new TandemModel(
                new ExponentialVariate(10, 2024),
                List.of(new ExponentialVariate(5, 1), new ExponentialVariate(6, 2), new ExponentialVariate(7, 3)));
        Report r = new Simulation(model, TerminationCondition.maxCompletions(200_000)).run();
        assertTrue(r.get("little_err") < 0.01, () -> "Little's Law error " + r.get("little_err"));
    }

    private static void assertRow(TraceRow row, double clock, String type, String state, String fel) {
        assertEquals(clock, row.clock(), EPS);
        assertEquals(type, row.eventType());
        assertEquals(state, row.state());
        assertEquals(fel, row.fel());
    }
}