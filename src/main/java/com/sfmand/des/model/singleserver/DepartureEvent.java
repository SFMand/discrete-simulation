package com.sfmand.des.model.singleserver;

import com.sfmand.des.engine.AbstractEvent;
import com.sfmand.des.engine.Simulation;

class DepartureEvent extends AbstractEvent {

    private final SingleServerModel model;
    private final Customer customer;

    DepartureEvent(double time, SingleServerModel model, Customer customer) {
        super(time);
        this.model = model;
        this.customer = customer;
    }

    @Override
    public String type() {
        return "D";
    }

    @Override
    public String typeName() {
        return "Departure";
    }

    @Override
    public void execute(Simulation sim) {
        model.numberOfCustomer--;
        model.completions++;
        model.totalSystemTime += sim.clock() - customer.arrivalTime();

        Customer next = model.queue.pollFirst();
        if (next != null) {
            model.startService(sim, next);
        } else {
            model.serverBusy = false;
        }
    }
}
