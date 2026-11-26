package com.sfmand.des.model.tandem;

import com.sfmand.des.engine.AbstractEvent;
import com.sfmand.des.engine.Simulation;

class ExternalArrivalEvent extends AbstractEvent {

    private final AbstractTandemModel model;

    ExternalArrivalEvent(double time, AbstractTandemModel model) {
        super(time);
        this.model = model;
    }

    @Override
    public String type() {
        return "A";
    }

    @Override
    public String typeName() {
        return "Arrival";
    }

    @Override
    public int priority() {
        return 1;
    }

    @Override
    public void execute(Simulation sim) {
        model.externalArrival(sim);
    }
}