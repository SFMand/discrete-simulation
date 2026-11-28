package com.sfmand.des.model.tandem;

import com.sfmand.des.engine.Report;
import com.sfmand.des.engine.Simulation;
import com.sfmand.des.model.network.Job;
import com.sfmand.des.model.network.Station;
import com.sfmand.des.random.Variate;

public class BlockingTandemModel extends AbstractTandemModel {

    private final int capacity;
    private final Station stage1;
    private final Station stage2;

    private int b;
    private long blockCount;
    private double blockedTime;

    public BlockingTandemModel(Variate interarrival, Variate service1, Variate service2, int capacityK) {
        this(interarrival, service1, service2, capacityK, Long.MAX_VALUE);
    }

    public BlockingTandemModel(Variate interarrival, Variate service1, Variate service2, int capacityK,
            long maxArrivals) {
        super(interarrival, maxArrivals);
        if (capacityK < 1) {
            throw new IllegalArgumentException("Stage 2 capacity K must be at least 1: " + capacityK);
        }
        this.capacity = capacityK;
        this.stage1 = new Station("S1", "ES1", "EndService 1", service1, -1);
        this.stage2 = new Station("S2", "D2", "Departure 2", service2, -2);
        stations.add(stage1);
        stations.add(stage2);
        stage1.onServiceEnd((sim, st) -> endServiceStage1(sim));
        stage2.onServiceEnd((sim, st) -> departureStage2(sim));
    }

    private void endServiceStage1(Simulation sim) {
        if (stage2.n() < capacity) {
            departStage1(sim);
        } else {
            b = 1;
            blockCount++;
        }
    }

    private void departStage1(Simulation sim) {
        Job job = stage1.release(sim);
        stage2.arrive(sim, job);
    }

    private void departureStage2(Simulation sim) {
        exit(sim, stage2.release(sim));
        if (b == 1) {
            b = 0;
            departStage1(sim);
        }
    }

    @Override
    public void updateStatistics(double dt) {
        super.updateStatistics(dt);
        if (b == 1) {
            blockedTime += dt;
        }
    }

    @Override
    public String name() {
        return "Tandem Queue with Blocking (K=" + capacity + ")";
    }

    public int blocked() {
        return b;
    }

    public long blockCount() {
        return blockCount;
    }

    public double blockedTime() {
        return blockedTime;
    }

    @Override
    public String stateSnapshot() {
        return "N=" + numberInSystem()
                + " | " + stage1.snapshot()
                + " | b=" + b
                + " | " + stage2.snapshot() + "/K=" + capacity;
    }

    @Override
    protected void addExtraMetrics(Report report, double finalClock) {
        report.add("blocks", "Times Server 1 became blocked", blockCount)
                .add("blocked", "Total time Server 1 was blocked", blockedTime)
                .add("P_blocked", "Fraction of time Server 1 blocked", ratio(blockedTime, finalClock))
                .add("U_1_eff", "Stage 1: utilization excl. blocked time",
                        ratio(stage1.busyTime() - blockedTime, finalClock));
    }
}