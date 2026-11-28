package com.sfmand.des;

import com.sfmand.des.engine.Report;
import com.sfmand.des.engine.Simulation;
import com.sfmand.des.engine.TerminationCondition;
import com.sfmand.des.engine.TraceRow;
import com.sfmand.des.model.tandem.BlockingTandemModel;
import com.sfmand.des.random.DeterministicSequence;
import com.sfmand.des.random.ExponentialVariate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockingTandemTask4Test {

    private static final double EPS = 1e-9;

    @BeforeEach
    void traceOn() { Simulation.TRACE_MODE = true; }

    @AfterEach
    void traceOff() { Simulation.TRACE_MODE = false; }

    private static BlockingTandemModel blockingModel() {
        return new BlockingTandemModel(
                new DeterministicSequence(1, 1),
                new DeterministicSequence(1, 1, 1),
                new DeterministicSequence(5, 1, 1),
                1, 3);
    }

    @Test
    void serverOneBlocksAndUnblocks() {
        Simulation sim = new Simulation(blockingModel(), TerminationCondition.never());
        sim.run();
        List<TraceRow> t = sim.trace();

        assertEquals(9, t.size());
        assertRow(t.get(0), 0, "Arrival", "N=1 | S1:n=1,Busy | b=0 | S2:n=0,Idle/K=1", "[(ES1, 1), (A, 1)]");
        assertRow(t.get(1), 1, "EndService 1", "N=1 | S1:n=0,Idle | b=0 | S2:n=1,Busy/K=1", "[(A, 1), (D2, 6)]");
        assertRow(t.get(2), 1, "Arrival", "N=2 | S1:n=1,Busy | b=0 | S2:n=1,Busy/K=1", "[(ES1, 2), (A, 2), (D2, 6)]");
        assertRow(t.get(3), 2, "EndService 1", "N=2 | S1:n=1,Busy | b=1 | S2:n=1,Busy/K=1", "[(A, 2), (D2, 6)]");
        assertRow(t.get(4), 2, "Arrival", "N=3 | S1:n=2,Busy | b=1 | S2:n=1,Busy/K=1", "[(D2, 6)]");
        assertRow(t.get(5), 6, "Departure 2", "N=2 | S1:n=1,Busy | b=0 | S2:n=1,Busy/K=1", "[(D2, 7), (ES1, 7)]");
        assertRow(t.get(6), 7, "Departure 2", "N=1 | S1:n=1,Busy | b=0 | S2:n=0,Idle/K=1", "[(ES1, 7)]");
        assertRow(t.get(7), 7, "EndService 1", "N=1 | S1:n=0,Idle | b=0 | S2:n=1,Busy/K=1", "[(D2, 8)]");
        assertRow(t.get(8), 8, "Departure 2", "N=0 | S1:n=0,Idle | b=0 | S2:n=0,Idle/K=1", "[]");
    }

    @Test
    void statisticsMatchHandComputation() {
        Report r = new Simulation(blockingModel(), TerminationCondition.never()).run();
        assertEquals(8, r.get("clock"), EPS);
        assertEquals(1, r.get("blocks"), EPS);
        assertEquals(4, r.get("blocked"), EPS);
        assertEquals(7.0 / 8, r.get("U_1"), EPS);
        assertEquals(3.0 / 8, r.get("U_1_eff"), EPS);
        assertEquals(18, r.get("sa"), EPS);
        assertEquals(6, r.get("W"), EPS);
        assertEquals(r.get("n_bar"), r.get("lambda_W"), EPS);
    }

    @Test
    void stageTwoNeverExceedsCapacity() {
        Simulation.TRACE_MODE = false;
        int k = 2;
        BlockingTandemModel model = new BlockingTandemModel(
                new ExponentialVariate(4, 1), new ExponentialVariate(3, 2), new ExponentialVariate(6, 3), k);
        TerminationCondition checkEveryStep = sim -> {
            int n2 = model.stations().get(1).n();
            if (n2 > k) {
                throw new AssertionError("Stage 2 holds " + n2 + " > K=" + k + " at clock " + sim.clock());
            }
            return model.completions() >= 50_000;
        };
        Report r = new Simulation(model, checkEveryStep).run();
        assertTrue(r.get("blocks") > 0, "expected some blocking with a slow Stage 2");
    }

    private static void assertRow(TraceRow row, double clock, String type, String state, String fel) {
        assertEquals(clock, row.clock(), EPS);
        assertEquals(type, row.eventType());
        assertEquals(state, row.state());
        assertEquals(fel, row.fel());
    }
}