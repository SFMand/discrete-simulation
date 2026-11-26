package com.sfmand.des;

import com.sfmand.des.engine.Report;
import com.sfmand.des.engine.Simulation;
import com.sfmand.des.engine.TerminationCondition;
import com.sfmand.des.engine.TraceRow;
import com.sfmand.des.model.finitepopulation.FinitePopulationAnalytical;
import com.sfmand.des.model.finitepopulation.FinitePopulationModel;
import com.sfmand.des.random.ExponentialVariate;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Long M/M/1//N run should approach the analytical steady-state values. */
class FinitePopulationRandomTest {

    private static final int TERMINALS = 5;
    private static final double MEAN_THINK = 20;
    private static final double MEAN_SERVICE = 4;
    private static final Pattern NUMBER_IN_SYSTEM = Pattern.compile("^n=(\\d+)");

    private static FinitePopulationModel model(double termSim) {
        return new FinitePopulationModel(TERMINALS,
                new ExponentialVariate(MEAN_THINK, 12345),
                new ExponentialVariate(MEAN_SERVICE, 67890),
                termSim);
    }

    @Test
    void convergesToAnalyticalValues() {
        Report report = new Simulation(model(1_000_000), TerminationCondition.never()).run();
        FinitePopulationAnalytical expected = FinitePopulationAnalytical.of(TERMINALS, MEAN_THINK, MEAN_SERVICE);

        assertRelative(expected.u(), report.get("U"), 0.03);
        assertRelative(expected.nBar(), report.get("n_bar"), 0.05);
        assertRelative(expected.bigW(), report.get("W"), 0.05);
        assertRelative(expected.w(), report.get("w"), 0.05);
    }

    @Test
    void satisfiesLittlesLaw() {
        Report report = new Simulation(model(1_000_000), TerminationCondition.never()).run();

        assertRelative(report.get("n_bar"), report.get("lambda_eff") * report.get("W"), 0.01);
    }

    @Test
    void terminalsAreConservedAtEveryEvent() {
        Simulation.TRACE_MODE = true;
        try {
            Simulation sim = new Simulation(model(1_000), TerminationCondition.never());
            sim.run();

            assertTrue(sim.trace().size() > 100, "run too short to test the invariant");
            for (TraceRow row : sim.trace()) {
                long thinking = row.felEntries().stream().filter(entry -> entry.startsWith("(A,")).count();
                assertEquals(TERMINALS, thinking + numberInSystem(row), () -> "row was " + row);
            }
        } finally {
            Simulation.TRACE_MODE = false;
        }
    }

    private static int numberInSystem(TraceRow row) {
        Matcher matcher = NUMBER_IN_SYSTEM.matcher(row.state());
        assertTrue(matcher.find(), () -> "state was " + row.state());
        return Integer.parseInt(matcher.group(1));
    }

    private static void assertRelative(double expected, double actual, double tolerance) {
        assertEquals(expected, actual, expected * tolerance);
    }
}
