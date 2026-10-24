package com.sfmand.des.engine;

public class EndOfSimulationEvent extends AbstractEvent {

    public EndOfSimulationEvent(double time) {
        super(time);
    }

    @Override
    public String type() {
        return "E";
    }

    @Override
    public String typeName() {
        return "End";
    }

    @Override
    public int priority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void execute(Simulation sim) {
        sim.stop();
    }
}
