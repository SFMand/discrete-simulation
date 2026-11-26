package com.sfmand.des;

import com.sfmand.des.engine.Report;
import com.sfmand.des.engine.Simulation;
import com.sfmand.des.engine.TerminationCondition;
import com.sfmand.des.engine.TraceRow;
import com.sfmand.des.model.finitepopulation.FinitePopulationModel;
import com.sfmand.des.random.DeterministicSequence;
import com.sfmand.des.random.Variate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** N = 2, think [1, 2, 4, 5, 6], service [3, 2, 1]. */
class FinitePopulationTask2Test {

    private static final double EPS = 1e-9;

    @BeforeEach
    void traceOn() {
        Simulation.TRACE_MODE = true;
    }

    @AfterEach
    void traceOff() {
        Simulation.TRACE_MODE = false;
    }

    private static FinitePopulationModel task2Model(double termSim) {
        return new FinitePopulationModel(2,
                new DeterministicSequence(1, 2, 4, 5, 6),
                new DeterministicSequence(3, 2, 1),
                termSim);
    }

    @Test
    void traceMatchesHandComputedRun() {
        Simulation sim = new Simulation(task2Model(10), TerminationCondition.never());
        Report report = sim.run();

        List<TraceRow> trace = sim.trace();
        assertEquals(7, trace.size());
        assertRow(trace.get(0), 1, "Arrival", "n=1, status=Busy, q=0", "[(A, 2), (D, 4), (E, 10)]");
        assertRow(trace.get(1), 2, "Arrival", "n=2, status=Busy, q=1", "[(D, 4), (E, 10)]");
        assertRow(trace.get(2), 4, "Departure", "n=1, status=Busy, q=0", "[(D, 6), (A, 8), (E, 10)]");
        assertRow(trace.get(3), 6, "Departure", "n=0, status=Idle, q=0", "[(A, 8), (E, 10), (A, 11)]");
        assertRow(trace.get(4), 8, "Arrival", "n=1, status=Busy, q=0", "[(D, 9), (E, 10), (A, 11)]");
        assertRow(trace.get(5), 9, "Departure", "n=0, status=Idle, q=0", "[(E, 10), (A, 11), (A, 15)]");
        assertRow(trace.get(6), 10, "End", "n=0, status=Idle, q=0", "[(A, 11), (A, 15)]");

        assertEquals(10, report.get("clock"), EPS);
        assertEquals(8, report.get("sa"), EPS);
        assertEquals(0.8, report.get("n_bar"), EPS);
        assertEquals(6, report.get("busy"), EPS);
        assertEquals(0.6, report.get("U"), EPS);
        assertEquals(2, report.get("sum_wq"), EPS);
        assertEquals(3, report.get("nw"), EPS);
        assertEquals(3, report.get("completions"), EPS);
        assertEquals(2.0 / 3, report.get("w"), EPS);
        assertEquals(8.0 / 3, report.get("W"), EPS);
        assertEquals(report.get("n_bar"), report.get("lambda_eff") * report.get("W"), EPS);
    }

    @Test
    void stopsMidServiceAtTermSim() {
        // Terminal 2 is still in service when the run ends at 5, so it counts in nw only.
        Simulation sim = new Simulation(task2Model(5), TerminationCondition.never());
        Report report = sim.run();

        assertRow(sim.trace().getLast(), 5, "End", "n=1, status=Busy, q=0", "[(D, 6), (A, 8)]");
        assertEquals(5, report.get("clock"), EPS);
        assertEquals(6, report.get("sa"), EPS);
        assertEquals(1.2, report.get("n_bar"), EPS);
        assertEquals(4, report.get("busy"), EPS);
        assertEquals(0.8, report.get("U"), EPS);
        assertEquals(2, report.get("nw"), EPS);
        assertEquals(1, report.get("completions"), EPS);
        assertEquals(2, report.get("sum_wq"), EPS);
        assertEquals(1, report.get("w"), EPS);
    }

    @Test
    void singleTerminalNeverWaits() {
        FinitePopulationModel model = new FinitePopulationModel(1,
                new DeterministicSequence(1, 1, 1, 1),
                new DeterministicSequence(2, 2, 2),
                9);
        Report report = new Simulation(model, TerminationCondition.never()).run();

        assertEquals(3, report.get("completions"), EPS);
        assertEquals(0, report.get("sum_wq"), EPS);
    }

    @Test
    void rejectsInvalidArguments() {
        Variate think = new DeterministicSequence(1);
        Variate service = new DeterministicSequence(1);

        assertThrows(IllegalArgumentException.class, () -> new FinitePopulationModel(0, think, service, 10));
        assertThrows(IllegalArgumentException.class, () -> new FinitePopulationModel(1, think, service, 0));
    }

    private static void assertRow(TraceRow row, double clock, String type, String statePrefix, String fel) {
        assertEquals(clock, row.clock(), EPS);
        assertEquals(type, row.eventType());
        assertEquals(true, row.state().startsWith(statePrefix), () -> "state was " + row.state());
        assertEquals(fel, row.fel());
    }
}
