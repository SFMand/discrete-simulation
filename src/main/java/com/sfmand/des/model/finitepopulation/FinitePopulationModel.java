package com.sfmand.des.model.finitepopulation;

import com.sfmand.des.engine.EndOfSimulationEvent;
import com.sfmand.des.engine.Model;
import com.sfmand.des.engine.Report;
import com.sfmand.des.engine.Simulation;
import com.sfmand.des.random.Variate;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Model 2: single server, finite population.
 * A closed loop of N terminals that alternate between thinking and the server.
 * The run ends when the clock reaches term_sim.
 */
public class FinitePopulationModel implements Model {

    final int terminals;
    final Variate think;
    final Variate service;
    final double termSim;

    // System state
    int numberOfCustomer;
    boolean serverBusy;
    final Deque<Customer> queue = new ArrayDeque<>();

    // Statistics
    long arrivals;
    long serviceStarts;
    long completions;
    double areaN;
    double busyTime;
    double totalQueueDelay;
    double totalSystemTime;

    public FinitePopulationModel(int terminals, Variate think, Variate service, double termSim) {
        if (terminals < 1) {
            throw new IllegalArgumentException("Number of terminals must be at least 1: " + terminals);
        }
        if (termSim <= 0) {
            throw new IllegalArgumentException("term_sim must be positive: " + termSim);
        }
        this.terminals = terminals;
        this.think = think;
        this.service = service;
        this.termSim = termSim;
    }

    @Override
    public String name() {
        return "Single Server, Finite Population";
    }

    @Override
    public void initialize(Simulation sim) {
        for (int j = 1; j <= terminals; j++) {
            sim.schedule(new ArrivalEvent(think.next(), this, j));
        }
        sim.schedule(new EndOfSimulationEvent(termSim));
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
        serviceStarts++;
        totalQueueDelay += sim.clock() - customer.arrivalTime();
        sim.schedule(new DepartureEvent(sim.clock() + service.next(), this, customer));
    }

    @Override
    public String stateSnapshot() {
        return "n=" + numberOfCustomer + ", status=" + (serverBusy ? "Busy" : "Idle")
                + ", q=" + queue.size() + ", thinking=" + (terminals - numberOfCustomer);
    }

    @Override
    public long completions() {
        return completions;
    }

    @Override
    public Report report(double finalClock) {
        return new Report(name())
                .add("N", "Number of terminals (N)", terminals)
                .add("clock", "Final simulation clock", finalClock)
                .add("arrivals", "Customers arrived", arrivals)
                .add("nw", "Customers started service (nw)", serviceStarts)
                .add("completions", "Customers completed", completions)
                .add("lambda_eff", "Effective throughput (lambda_eff)", ratio(completions, finalClock))
                .add("sum_wq", "Sum of waiting times in queue", totalQueueDelay)
                .add("w", "Average waiting time in queue (w)", ratio(totalQueueDelay, serviceStarts))
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
