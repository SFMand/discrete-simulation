package com.sfmand.des.model.network;

public final class Job {

    private final long id;
    private final double systemArrival;
    private double stationArrival;

    public Job(long id, double systemArrival) {
        this.id = id;
        this.systemArrival = systemArrival;
        this.stationArrival = systemArrival;
    }

    public long id() {
        return id;
    }

    public double systemArrival() {
        return systemArrival;
    }

    public double stationArrival() {
        return stationArrival;
    }

    void enterStation(double time) {
        this.stationArrival = time;
    }

    @Override
    public String toString() {
        return "Job" + id;
    }
}