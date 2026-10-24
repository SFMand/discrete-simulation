package com.sfmand.des.model.singleserver;

import com.sfmand.des.engine.AbstractEvent;
import com.sfmand.des.engine.Simulation;

class ArrivalEvent extends AbstractEvent {

    private final SingleServerModel model;

    ArrivalEvent(double time, SingleServerModel model) {
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
        Customer customer = new Customer(++model.arrivals, sim.clock());
        model.numberOfCustomer++;

        if (model.arrivals < model.maxArrivals && model.interarrival.hasNext()) {
            sim.schedule(new ArrivalEvent(sim.clock() + model.interarrival.next(), model));
        }

        if (model.serverBusy) {
            model.queue.addLast(customer);
        } else {
            model.startService(sim, customer);
        }
    }
}
