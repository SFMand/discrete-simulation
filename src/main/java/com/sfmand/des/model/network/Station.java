package com.sfmand.des.model.network;

import com.sfmand.des.engine.Simulation;
import com.sfmand.des.random.Variate;

import java.util.ArrayDeque;
import java.util.Deque;

public class Station {

    @FunctionalInterface
    public interface ServiceEndHandler {
        void onServiceEnd(Simulation sim, Station station);
    }

    private final String name;
    private final String eventLabel;
    private final String eventName;
    private final Variate service;
    private final int eventPriority;
    private ServiceEndHandler handler;

    // State
    private int n;
    private Job inService;
    private final Deque<Job> queue = new ArrayDeque<>();

    // Statistics
    private double area;
    private double busyTime;
    private double sumWait;
    private long serviceStarts;
    private long releases;

    public Station(String name, String eventLabel, String eventName, Variate service, int eventPriority) {
        this.name = name;
        this.eventLabel = eventLabel;
        this.eventName = eventName;
        this.service = service;
        this.eventPriority = eventPriority;
    }

    public Station onServiceEnd(ServiceEndHandler handler) {
        this.handler = handler;
        return this;
    }

    public void updateStatistics(double dt) {
        area += n * dt;
        if (inService != null) {
            busyTime += dt;
        }
    }

    public void arrive(Simulation sim, Job job) {
        job.enterStation(sim.clock());
        n++;
        if (inService == null) {
            startService(sim, job);
        } else {
            queue.addLast(job);
        }
    }

    private void startService(Simulation sim, Job job) {
        inService = job;
        serviceStarts++;
        sumWait += sim.clock() - job.stationArrival();
        sim.schedule(new ServiceEndEvent(sim.clock() + service.next(), this));
    }

    public Job release(Simulation sim) {
        if (inService == null) {
            throw new IllegalStateException(name + " has no job in service to release");
        }
        Job done = inService;
        inService = null;
        n--;
        releases++;
        Job next = queue.pollFirst();
        if (next != null) {
            startService(sim, next);
        }
        return done;
    }

    void serviceEnded(Simulation sim) {
        if (handler == null) {
            throw new IllegalStateException("No service-end handler configured for " + name);
        }
        handler.onServiceEnd(sim, this);
    }

    public String name() { return name; }
    String eventLabel() { return eventLabel; }
    String eventName() { return eventName; }
    int eventPriority() { return eventPriority; }
    public int n() { return n; }
    public boolean isBusy() { return inService != null; }
    public int queueLength() { return queue.size(); }
    public long serviceStarts() { return serviceStarts; }
    public long releases() { return releases; }
    public double area() { return area; }
    public double busyTime() { return busyTime; }
    public double sumWait() { return sumWait; }

    public String snapshot() {
        return name + ":n=" + n + "," + (inService != null ? "Busy" : "Idle");
    }
}