package com.sfmand.des.model.tandem;

import com.sfmand.des.engine.Model;
import com.sfmand.des.engine.Report;
import com.sfmand.des.engine.Simulation;
import com.sfmand.des.engine.TerminationCondition;
import com.sfmand.des.model.network.Job;
import com.sfmand.des.model.network.Station;
import com.sfmand.des.random.Variate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractTandemModel implements Model {

    private final Variate interarrival;
    private final long maxArrivals;
    protected final List<Station> stations = new ArrayList<>();

    private long arrivals;
    private long exits;
    private double totalSystemTime;

    AbstractTandemModel(Variate interarrival, long maxArrivals) {
        this.interarrival = interarrival;
        this.maxArrivals = maxArrivals;
    }

    @Override
    public void initialize(Simulation sim) {
        sim.schedule(new ExternalArrivalEvent(0.0, this));
    }

    void externalArrival(Simulation sim) {
        Job job = new Job(++arrivals, sim.clock());
        if (arrivals < maxArrivals && interarrival.hasNext()) {
            sim.schedule(new ExternalArrivalEvent(sim.clock() + interarrival.next(), this));
        }
        stations.get(0).arrive(sim, job);
    }

    protected void exit(Simulation sim, Job job) {
        exits++;
        totalSystemTime += sim.clock() - job.systemArrival();
    }

    @Override
    public void updateStatistics(double dt) {
        for (Station station : stations) {
            station.updateStatistics(dt);
        }
    }

    @Override
    public long completions() {
        return exits;
    }

    public long arrivals() {
        return arrivals;
    }

    public int numberInSystem() {
        int total = 0;
        for (Station station : stations) {
            total += station.n();
        }
        return total;
    }

    public List<Station> stations() {
        return Collections.unmodifiableList(stations);
    }

    public TerminationCondition untilServiceStarts(long m) {
        return sim -> stations.get(0).serviceStarts() >= m;
    }

    @Override
    public String stateSnapshot() {
        StringBuilder sb = new StringBuilder("N=").append(numberInSystem());
        for (Station station : stations) {
            sb.append(" | ").append(station.snapshot());
        }
        return sb.toString();
    }

    protected void addExtraMetrics(Report report, double finalClock) {
    }

    @Override
    public Report report(double finalClock) {
        double areaTotal = 0;
        for (Station station : stations) {
            areaTotal += station.area();
        }
        double lambda = ratio(arrivals, finalClock);
        double w = ratio(totalSystemTime, exits);
        double nBar = ratio(areaTotal, finalClock);

        Report report = new Report(name())
                .add("clock", "Final simulation clock", finalClock)
                .add("arrivals", "Customers arrived", arrivals)
                .add("completions", "Customers that left the last stage", exits)
                .add("in_system", "Customers still inside at the end", numberInSystem());

        for (int i = 0; i < stations.size(); i++) {
            Station s = stations.get(i);
            int k = i + 1;
            report.add("w_" + k, "Stage " + k + ": average wait in queue", ratio(s.sumWait(), s.serviceStarts()))
                    .add("n_bar_" + k, "Stage " + k + ": time-average number (n_bar)", ratio(s.area(), finalClock))
                    .add("U_" + k, "Stage " + k + ": utilization (U)", ratio(s.busyTime(), finalClock));
        }

        addExtraMetrics(report, finalClock);

        report.add("sa", "Sum of state areas, all stages (sa)", areaTotal)
                .add("n_bar", "Time-average number in system (n_bar)", nBar)
                .add("lambda", "Arrival rate (arrivals / clock)", lambda)
                .add("W", "Average time in system (W)", w)
                .add("lambda_W", "Little's Law: lambda * W", lambda * w)
                .add("little_err", "Relative error |n_bar - lambda*W| / n_bar",
                        nBar == 0 ? 0 : Math.abs(nBar - lambda * w) / nBar);
        return report;
    }

    static double ratio(double numerator, double denominator) {
        return denominator == 0 ? 0.0 : numerator / denominator;
    }
}