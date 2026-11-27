package com.sfmand.des.model.closednetwork;

import com.sfmand.des.engine.EndOfSimulationEvent;
import com.sfmand.des.engine.Model;
import com.sfmand.des.engine.Report;
import com.sfmand.des.engine.Simulation;
import com.sfmand.des.model.network.Job;
import com.sfmand.des.model.network.Station;
import com.sfmand.des.random.Variate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/** Task 5: N permanent jobs circulate through four single-server stations. */
public class ClosedNetworkModel implements Model {

    private final int population;
    private final double termSim;
    private final Variate routing;
    private final List<Station> stations = new ArrayList<>();
    private final long[] routes = new long[3];
    private final double[] completedWait = new double[4];
    private long completedVisits;
    private long cycles;

    public ClosedNetworkModel(int population, List<? extends Variate> serviceTimes,
            long routingSeed, double termSim) {
        this(population, serviceTimes, new Random(routingSeed)::nextDouble, termSim);
    }

    /** Inject uniform draws in [0,1) to make routing deterministic in tests. */
    public ClosedNetworkModel(int population, List<? extends Variate> serviceTimes,
            Variate routing, double termSim) {
        if (population < 1) {
            throw new IllegalArgumentException("Population N must be at least 1");
        }
        if (!Double.isFinite(termSim) || termSim <= 0) {
            throw new IllegalArgumentException("term_sim must be finite and positive");
        }
        if (serviceTimes.size() != 4) {
            throw new IllegalArgumentException("The closed network requires exactly four servers");
        }
        this.population = population;
        this.termSim = termSim;
        this.routing = Objects.requireNonNull(routing, "routing");
        for (int i = 0; i < 4; i++) {
            int index = i;
            int k = i + 1;
            Variate service = Objects.requireNonNull(serviceTimes.get(i), "service time");
            Station station = new Station("S" + k, "D" + k, "Departure " + k, () -> {
                double time = service.next();
                if (!Double.isFinite(time) || time <= 0) {
                    throw new IllegalArgumentException("Service duration must be finite and positive");
                }
                return time;
            }, -k);
            station.onServiceEnd((sim, st) -> depart(sim, index));
            stations.add(station);
        }
    }

    @Override
    public String name() {
        return "Closed Network (N=" + population + ")";
    }

    @Override
    public void initialize(Simulation sim) {
        // All N jobs enter S1 at clock 0; one starts service and N-1 wait.
        for (int j = 1; j <= population; j++) {
            stations.get(0).arrive(sim, new Job(j, 0));
        }
        sim.schedule(new EndOfSimulationEvent(termSim));
    }

    private void depart(Simulation sim, int index) {
        int destination = 0;
        if (index == 0) {
            double u = routing.next();
            if (!Double.isFinite(u) || u < 0 || u >= 1) {
                throw new IllegalArgumentException("Routing draw must be in [0,1)");
            }
            destination = u < 0.5 ? 1 : u < 0.8 ? 2 : 3;
            routes[destination - 1]++;
        } else {
            cycles++;
        }
        Station source = stations.get(index);
        // Before release starts the next job, all waits recorded at this station
        // belong to visits that have now completed. Exclude unfinished visits.
        completedWait[index] = source.sumWait();
        Job job = source.release(sim);
        completedVisits++;
        stations.get(destination).arrive(sim, job);
    }

    @Override
    public void updateStatistics(double dt) {
        for (Station station : stations) {
            station.updateStatistics(dt);
        }
    }

    /** Completed server visits, not jobs leaving this closed system. */
    @Override
    public long completions() {
        return completedVisits;
    }

    public int numberInSystem() {
        return stations.stream().mapToInt(Station::n).sum();
    }

    public List<Station> stations() {
        return Collections.unmodifiableList(stations);
    }

    @Override
    public String stateSnapshot() {
        StringBuilder state = new StringBuilder("N=").append(numberInSystem());
        for (Station station : stations) {
            state.append(" | ").append(station.snapshot());
        }
        return state.toString();
    }

    @Override
    public Report report(double finalClock) {
        Report report = new Report(name())
                .add("N", "Permanent population (N)", population)
                .add("clock", "Final simulation clock", finalClock)
                .add("in_system", "Jobs still in the network", numberInSystem())
                .add("completions", "Completed server visits", completedVisits)
                .add("cycles", "Completed returns to Server 1", cycles);
        double totalArea = 0;
        double totalWait = 0;
        for (int i = 0; i < 4; i++) {
            Station s = stations.get(i);
            int k = i + 1;
            totalArea += s.area();
            totalWait += completedWait[i];
            report.add("completions_" + k, "S" + k + ": completed visits", s.releases())
                    .add("w_" + k, "S" + k + ": mean queue wait per completed visit",
                            ratio(completedWait[i], s.releases()))
                    .add("n_bar_" + k, "S" + k + ": time-average number", ratio(s.area(), finalClock))
                    .add("U_" + k, "S" + k + ": server utilization", ratio(s.busyTime(), finalClock));
        }
        report.add("sa", "Sum of state areas", totalArea)
                .add("n_bar", "Time-average number in network (equals N)", ratio(totalArea, finalClock))
                .add("w", "Mean queue wait per completed server visit", ratio(totalWait, completedVisits));
        for (int i = 0; i < 3; i++) {
            int k = i + 2;
            report.add("routes_1_" + k, "Transfers S1 -> S" + k, routes[i])
                    .add("p_1_" + k, "Observed routing fraction S1 -> S" + k,
                            ratio(routes[i], stations.get(0).releases()));
        }
        return report;
    }

    private static double ratio(double numerator, double denominator) {
        return denominator == 0 ? 0 : numerator / denominator;
    }
}
