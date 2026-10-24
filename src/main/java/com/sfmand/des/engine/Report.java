package com.sfmand.des.engine;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/** Named performance metrics produced at the end of a run */
public class Report {

    public record Metric(String key, String description, double value) {
    }

    private final String title;
    private final List<Metric> metrics = new ArrayList<>();

    public Report(String title) {
        this.title = title;
    }

    public Report add(String key, String description, double value) {
        metrics.add(new Metric(key, description, value));
        return this;
    }

    public double get(String key) {
        for (Metric metric : metrics) {
            if (metric.key().equals(key)) {
                return metric.value();
            }
        }
        throw new NoSuchElementException("No metric named " + key);
    }

    public List<Metric> metrics() {
        return Collections.unmodifiableList(metrics);
    }

    public void print() {
        print(System.out);
    }

    public void print(PrintStream out) {
        out.println("=== Report: " + title + " ===");
        for (Metric metric : metrics) {
            out.printf("  %-12s %-42s %12s%n", metric.key(), metric.description(),
                    Numbers.format(metric.value(), 4));
        }
    }
}
