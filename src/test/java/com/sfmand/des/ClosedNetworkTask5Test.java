package com.sfmand.des;

import com.sfmand.des.engine.Report;
import com.sfmand.des.engine.Simulation;
import com.sfmand.des.engine.TerminationCondition;
import com.sfmand.des.model.closednetwork.ClosedNetworkModel;
import com.sfmand.des.model.network.Station;
import com.sfmand.des.random.DeterministicSequence;
import com.sfmand.des.random.ExponentialVariate;
import com.sfmand.des.random.Variate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClosedNetworkTask5Test {
    private static final double EPS = 1e-9;

    @AfterEach
    void traceOff() { Simulation.TRACE_MODE = false; }

    private static List<Variate> constantServices() {
        return List.of(() -> 2, () -> 3, () -> 4, () -> 5);
    }

    @Test
    void initializationPlacesAllJobsAtServerOneWithoutExternalArrivals() {
        ClosedNetworkModel model = new ClosedNetworkModel(5, constantServices(), 42, 20);
        Simulation sim = new Simulation(model, TerminationCondition.maxEvents(0));
        sim.run();
        assertEquals(5, model.numberInSystem());
        assertEquals(4, model.stations().get(0).queueLength());
        assertTrue(model.stations().get(0).isBusy());
        for (int i = 1; i < 4; i++) assertEquals(0, model.stations().get(i).n());
        assertEquals("[(D1, 2), (E, 20)]", sim.fel().toString());
    }

    @Test
    void traceAndStatisticsMatchHandCalculation() {
        Simulation.TRACE_MODE = true;
        ClosedNetworkModel model = new ClosedNetworkModel(2, constantServices(),
                new DeterministicSequence(0.1, 0.6, 0.9, 0.1), 12);
        Simulation sim = new Simulation(model, TerminationCondition.never());
        Report r = sim.run();
        assertEquals(List.of(2.0, 4.0, 5.0, 7.0, 8.0, 10.0, 12.0, 12.0),
                sim.trace().stream().map(row -> row.clock()).toList());
        assertEquals(List.of("Departure 1", "Departure 1", "Departure 2", "Departure 1",
                "Departure 3", "Departure 1", "Departure 4", "End"),
                sim.trace().stream().map(row -> row.eventType()).toList());
        assertEquals("N=2 | S1:n=1,Busy | S2:n=1,Busy | S3:n=0,Idle | S4:n=0,Idle",
                sim.trace().get(0).state());
        assertEquals("[(D1, 4), (D2, 5), (E, 12)]", sim.trace().get(0).fel());
        assertEquals("N=2 | S1:n=0,Idle | S2:n=0,Idle | S3:n=1,Busy | S4:n=1,Busy",
                sim.trace().get(3).state());
        assertEquals(12, r.get("clock"), EPS);
        assertEquals(24, r.get("sa"), EPS);
        assertEquals(2, r.get("n_bar"), EPS);
        assertEquals(7, r.get("completions"), EPS);
        assertEquals(3, r.get("cycles"), EPS);
        assertEquals(2.0 / 7, r.get("w"), EPS);
        assertEquals(0.5, r.get("w_1"), EPS);
        double[] areas = {10, 5, 4, 5};
        double[] active = {8, 5, 4, 5};
        for (int i = 0; i < 4; i++) {
            assertEquals(areas[i] / 12, r.get("n_bar_" + (i + 1)), EPS);
            assertEquals(active[i] / 12, r.get("U_" + (i + 1)), EPS);
        }
        assertEquals(2, r.get("routes_1_2"), EPS);
        assertEquals(1, r.get("routes_1_3"), EPS);
        assertEquals(1, r.get("routes_1_4"), EPS);
    }

    @Test
    void exactRoutingThresholdsAndAllReturnPaths() {
        double[] draws = {0, Math.nextDown(0.5), 0.5, Math.nextDown(0.8), 0.8, Math.nextDown(1.0)};
        int[] destinations = {1, 1, 2, 2, 3, 3};
        for (int i = 0; i < draws.length; i++) {
            ClosedNetworkModel model = new ClosedNetworkModel(1,
                    List.of(() -> 1, () -> 1, () -> 1, () -> 1),
                    new DeterministicSequence(draws[i]), 2);
            int destination = destinations[i];
            Simulation sim = new Simulation(model, s -> {
                if (s.eventCount() == 1) {
                    assertEquals(1, model.stations().get(destination).n());
                    assertEquals(0, model.stations().get(0).n());
                }
                return false;
            });
            Report r = sim.run();
            assertEquals(1, model.stations().get(0).n());
            assertEquals(1, r.get("cycles"), EPS);
            assertEquals(2, r.get("completions"), EPS);
        }
    }

    @Test
    void waitingMetricExcludesUnfinishedVisit() {
        ClosedNetworkModel model = new ClosedNetworkModel(3,
                List.of(() -> 0.5, () -> 10, () -> 10, () -> 10), () -> 0, 1);
        Report r = new Simulation(model, TerminationCondition.never()).run();
        // Job 2 waited 0.5 and completed; Job 3 waited 1 but is still in service.
        assertEquals(2, r.get("completions"), EPS);
        assertEquals(0.25, r.get("w_1"), EPS);
        assertEquals(0.25, r.get("w"), EPS);
    }

    @Test
    void longRunConservesPopulationAndMatchesRoutingProbabilities() {
        ClosedNetworkModel model = new ClosedNetworkModel(10,
                List.of(new ExponentialVariate(2, 7), new ExponentialVariate(3, 11),
                        new ExponentialVariate(4, 13), new ExponentialVariate(5, 17)),
                42, 1_000_000);
        Report r = new Simulation(model, sim -> {
            assertEquals(10, model.numberInSystem());
            for (Station s : model.stations()) {
                assertEquals(s.n(), s.queueLength() + (s.isBusy() ? 1 : 0));
            }
            assertTrue(sim.fel().snapshot().stream().allMatch(e ->
                    e.type().equals("E") || e.type().matches("D[1-4]")));
            return model.completions() >= 200_000;
        }).run();
        assertEquals(200_000, r.get("completions"), EPS);
        assertEquals(10, r.get("n_bar"), 1e-8);
        assertEquals(0.5, r.get("p_1_2"), 0.01);
        assertEquals(0.3, r.get("p_1_3"), 0.01);
        assertEquals(0.2, r.get("p_1_4"), 0.01);
        for (int k = 1; k <= 4; k++) {
            assertTrue(r.get("U_" + k) >= 0 && r.get("U_" + k) <= 1);
            assertTrue(r.get("w_" + k) >= 0);
        }
    }

    @Test
    void rejectsInvalidConfigurationAndDraws() {
        assertThrows(IllegalArgumentException.class,
                () -> new ClosedNetworkModel(0, constantServices(), 42, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new ClosedNetworkModel(1, List.of(() -> 1), 42, 10));
        for (double horizon : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class,
                    () -> new ClosedNetworkModel(1, constantServices(), 42, horizon));
        }
        for (double draw : new double[]{-0.1, 1, Double.NaN}) {
            ClosedNetworkModel model = new ClosedNetworkModel(1, constantServices(), () -> draw, 10);
            assertThrows(IllegalArgumentException.class,
                    () -> new Simulation(model, TerminationCondition.never()).run());
        }
    }
}
