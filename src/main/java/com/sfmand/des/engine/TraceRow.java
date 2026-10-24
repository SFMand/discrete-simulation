package com.sfmand.des.engine;

import java.util.List;
import java.util.stream.Collectors;

/**
 * One state-trace snapshot: [CLOCK] | [Event Type] | [System State] | [FEL
 * entries].
 */
public record TraceRow(double clock, String eventType, String state, List<String> felEntries) {

    static final String HEADER = String.format("%8s | %-10s | %-30s | %s",
            "CLOCK", "EVENT", "SYSTEM STATE", "FEL");

    static TraceRow capture(double clock, Event event, String state, FutureEventList fel) {
        List<String> entries = fel.snapshot().stream()
                .map(e -> "(" + e.type() + ", " + Numbers.format(e.time()) + ")")
                .toList();
        return new TraceRow(clock, event.typeName(), state, entries);
    }

    public String fel() {
        return felEntries.stream().collect(Collectors.joining(", ", "[", "]"));
    }

    @Override
    public String toString() {
        return String.format("%8s | %-10s | %-30s | %s", Numbers.format(clock), eventType, state, fel());
    }
}
