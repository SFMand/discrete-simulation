package com.sfmand.des.engine;

/**
 * Stop rule checked before each event is taken from the FEL. The run also ends
 * when the FEL is empty or an event calls {@link Simulation#stop()}.
 */
@FunctionalInterface
public interface TerminationCondition {

    boolean isMet(Simulation sim);

    static TerminationCondition never() {
        return sim -> false;
    }

    static TerminationCondition maxEvents(long events) {
        return sim -> sim.eventCount() >= events;
    }

    static TerminationCondition maxCompletions(long completions) {
        return sim -> sim.model().completions() >= completions;
    }

    static TerminationCondition anyOf(TerminationCondition... conditions) {
        return sim -> {
            for (TerminationCondition condition : conditions) {
                if (condition.isMet(sim)) {
                    return true;
                }
            }
            return false;
        };
    }
}
