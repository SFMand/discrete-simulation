package com.sfmand.des.model.finitepopulation;

import com.sfmand.des.engine.AbstractEvent;
import com.sfmand.des.engine.Simulation;

/** A customer leaves the server and its terminal goes back to thinking. */
class DepartureEvent extends AbstractEvent {

    private final FinitePopulationModel model;
    private final Customer customer;

    DepartureEvent(double time, FinitePopulationModel model, Customer customer) {
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

        sim.schedule(new ArrivalEvent(sim.clock() + model.think.next(), model, customer.terminal()));

        Customer next = model.queue.pollFirst();
        if (next != null) {
            model.startService(sim, next);
        } else {
            model.serverBusy = false;
        }
    }
}
