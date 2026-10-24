package com.sfmand.des.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class FutureEventList {

    public static final Comparator<Event> EVENT_ORDER = Comparator
            .comparingDouble(Event::time)
            .thenComparingInt(Event::priority)
            .thenComparingLong(Event::sequence);

    private final PriorityQueue<Event> queue = new PriorityQueue<>(EVENT_ORDER);

    public void schedule(Event event) {
        queue.add(event);
    }

    public Event poll() {
        return queue.poll();
    }

    public Event peek() {
        return queue.peek();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }

    public List<Event> snapshot() {
        List<Event> events = new ArrayList<>(queue);
        events.sort(EVENT_ORDER);
        return events;
    }

    @Override
    public String toString() {
        return snapshot().toString();
    }
}
