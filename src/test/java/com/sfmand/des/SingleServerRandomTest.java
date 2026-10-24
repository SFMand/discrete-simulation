package com.sfmand.des;

import com.sfmand.des.engine.Report;
import com.sfmand.des.engine.Simulation;
import com.sfmand.des.engine.TerminationCondition;
import com.sfmand.des.model.singleserver.SingleServerModel;
import com.sfmand.des.random.ExponentialVariate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Long M/M/1 run should approach the analytical steady-state values. */
class SingleServerRandomTest {

    @Test
    void mm1ConvergesToAnalyticalValues() {
        double meanInterarrival = 10;
        double meanService = 5;
        SingleServerModel model = new SingleServerModel(
                new ExponentialVariate(meanInterarrival, 12345),
                new ExponentialVariate(meanService, 67890));

        Report report = new Simulation(model, TerminationCondition.maxCompletions(200_000)).run();

        double rho = meanService / meanInterarrival;   // 0.5
        double systemTime = meanService / (1 - rho);   // W  = 10
        double queueDelay = systemTime - meanService;  // w  = 5
        double numberInSystem = rho / (1 - rho);       // L  = 1

        assertRelative(rho, report.get("U"), 0.03);
        assertRelative(numberInSystem, report.get("n_bar"), 0.05);
        assertRelative(systemTime, report.get("W"), 0.05);
        assertRelative(queueDelay, report.get("w"), 0.05);
    }

    private static void assertRelative(double expected, double actual, double tolerance) {
        assertEquals(expected, actual, expected * tolerance);
    }
}
