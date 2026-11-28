package com.sfmand.des.model.network;

import com.sfmand.des.engine.AbstractEvent;
import com.sfmand.des.engine.Simulation;

class ServiceEndEvent extends AbstractEvent {

    private final Station station;

    ServiceEndEvent(double time, Station station) {
        super(time);
        this.station = station;
    }

    @Override
    public String type() {
        return station.eventLabel();
    }

    @Override
    public String typeName() {
        return station.eventName();
    }

    @Override
    public int priority() {
        return station.eventPriority();
    }

    @Override
    public void execute(Simulation sim) {
        station.serviceEnded(sim);
    }
}