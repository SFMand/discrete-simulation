package com.sfmand.des.model.singleserver;

import com.sfmand.des.engine.Model;
import com.sfmand.des.engine.Report;
import com.sfmand.des.engine.Simulation;
import com.sfmand.des.random.Variate;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Model 1: single server, infinite population
 * The first customer arrives at time 0
 */
public class SingleServerModel implements Model {

    final Variate interarrival;
    final Variate service;
    final long maxArrivals;

    // System state
    int numberOfCustomer;
    boolean serverBusy;
    final Deque<Customer> queue = new ArrayDeque<>();

    // Statistics
    long arrivals;
    long completions;
    double areaN;
    double busyTime;
    double totalQueueDelay;
    double totalSystemTime;

    public SingleServerModel(Variate interarrival, Variate service) {
        this(interarrival, service, Long.MAX_VALUE);
    }

    public SingleServerModel(Variate interarrival, Variate service, long maxArrivals) {
        this.interarrival = interarrival;
        this.service = service;
        this.maxArrivals = maxArrivals;
    }

    @Override
    public String name() {
        return "Single Server, Infinite Population";
    }

    @Override
    public void initialize(Simulation sim) {
        sim.schedule(new ArrivalEvent(0.0, this));
    }

    @Override
    public void updateStatistics(double dt) {
        areaN += numberOfCustomer * dt;
        if (serverBusy) {
            busyTime += dt;
        }
    }

    void startService(Simulation sim, Customer customer) {
        serverBusy = true;
        totalQueueDelay += sim.clock() - customer.arrivalTime();
        sim.schedule(new DepartureEvent(sim.clock() + service.next(), this, customer));
    }

    @Override
    public String stateSnapshot() {
        return "n=" + numberOfCustomer + ", status=" + (serverBusy ? "Busy" : "Idle") + ", q=" + queue.size();
    }

    @Override
    public long completions() {
        return completions;
    }

    public int numberInSystem() {
        return numberOfCustomer;
    }

    public boolean isServerBusy() {
        return serverBusy;
    }

    @Override
    public Report report(double finalClock) {
        return new Report(name())
                .add("clock", "Final simulation clock", finalClock)
                .add("arrivals", "Customers arrived", arrivals)
                .add("completions", "Customers completed", completions)
                .add("sum_wq", "Sum of waiting times in queue", totalQueueDelay)
                .add("w", "Average waiting time in queue (w)", ratio(totalQueueDelay, completions))
                .add("W", "Average time in system (W)", ratio(totalSystemTime, completions))
                .add("sa", "Sum of state areas (sa)", areaN)
                .add("n_bar", "Time-average number in system (n_bar)", ratio(areaN, finalClock))
                .add("busy", "Total server active time", busyTime)
                .add("U", "Server utilization (U)", ratio(busyTime, finalClock));
    }

    private static double ratio(double numerator, double denominator) {
        return denominator == 0 ? 0.0 : numerator / denominator;
    }
}
