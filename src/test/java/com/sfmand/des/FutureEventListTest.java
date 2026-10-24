package com.sfmand.des;

import com.sfmand.des.engine.AbstractEvent;
import com.sfmand.des.engine.Event;
import com.sfmand.des.engine.FutureEventList;
import com.sfmand.des.engine.Model;
import com.sfmand.des.engine.Report;
import com.sfmand.des.engine.Simulation;
import com.sfmand.des.engine.TerminationCondition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FutureEventListTest {

    private static class TestEvent extends AbstractEvent {
        private final String label;
        private final int priority;

        TestEvent(double time, String label, int priority) {
            super(time);
            this.label = label;
            this.priority = priority;
        }

        @Override
        public String type() {
            return label;
        }

        @Override
        public String typeName() {
            return label;
        }

        @Override
        public int priority() {
            return priority;
        }

        @Override
        public void execute(Simulation sim) {
        }
    }

    @Test
    void pollsInTimeOrder() {
        FutureEventList fel = new FutureEventList();
        Event late = new TestEvent(9, "late", 0);
        Event early = new TestEvent(2, "early", 0);
        Event middle = new TestEvent(5, "middle", 0);
        fel.schedule(late);
        fel.schedule(early);
        fel.schedule(middle);

        assertSame(early, fel.poll());
        assertSame(middle, fel.poll());
        assertSame(late, fel.poll());
        assertEquals(null, fel.poll());
    }

    @Test
    void tiesBreakOnPriorityThenSchedulingOrder() {
        FutureEventList fel = new FutureEventList();
        Event firstLowPriority = new TestEvent(4, "a1", 1);
        Event secondLowPriority = new TestEvent(4, "a2", 1);
        Event highPriority = new TestEvent(4, "d", 0);
        fel.schedule(firstLowPriority);
        fel.schedule(secondLowPriority);
        fel.schedule(highPriority);

        assertSame(highPriority, fel.poll());
        assertSame(firstLowPriority, fel.poll());
        assertSame(secondLowPriority, fel.poll());
    }

    @Test
    void snapshotIsSortedAndLeavesListIntact() {
        FutureEventList fel = new FutureEventList();
        for (double t : new double[] {7, 3, 11, 1, 5}) {
            fel.schedule(new TestEvent(t, "x", 0));
        }

        List<Event> snapshot = fel.snapshot();

        assertEquals(List.of(1.0, 3.0, 5.0, 7.0, 11.0), snapshot.stream().map(Event::time).toList());
        assertEquals(5, fel.size());
    }

    @Test
    void simulationRejectsEventsInThePast() {
        Model model = new Model() {
            @Override
            public String name() {
                return "past";
            }

            @Override
            public void initialize(Simulation sim) {
                sim.schedule(new TestEvent(5, "x", 0) {
                    @Override
                    public void execute(Simulation s) {
                        s.schedule(new TestEvent(2, "y", 0));
                    }
                });
            }

            @Override
            public void updateStatistics(double dt) {
            }

            @Override
            public String stateSnapshot() {
                return "";
            }

            @Override
            public long completions() {
                return 0;
            }

            @Override
            public Report report(double finalClock) {
                return new Report(name());
            }
        };

        Simulation sim = new Simulation(model, TerminationCondition.never());
        assertThrows(IllegalStateException.class, sim::run);
    }
}
