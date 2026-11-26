package com.sfmand.des.model.finitepopulation;

import com.sfmand.des.engine.AbstractEvent;
import com.sfmand.des.engine.Simulation;

/** A terminal finishes thinking and joins the system. No next arrival is scheduled here. */
class ArrivalEvent extends AbstractEvent {

    private final FinitePopulationModel model;
    private final int terminal;

    ArrivalEvent(double time, FinitePopulationModel model, int terminal) {
        super(time);
        this.model = model;
        this.terminal = terminal;
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
        Customer customer = new Customer(terminal, sim.clock());
        model.arrivals++;
        model.numberOfCustomer++;

        if (model.serverBusy) {
            model.queue.addLast(customer);
        } else {
            model.startService(sim, customer);
        }
    }
}
