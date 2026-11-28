package com.sfmand.des;

import java.util.List;

import com.sfmand.des.engine.Numbers;
import com.sfmand.des.engine.Report;
import com.sfmand.des.engine.Simulation;
import com.sfmand.des.engine.TerminationCondition;
import com.sfmand.des.model.finitepopulation.FinitePopulationAnalytical;
import com.sfmand.des.model.finitepopulation.FinitePopulationModel;
import com.sfmand.des.model.closednetwork.ClosedNetworkModel;
import com.sfmand.des.model.singleserver.SingleServerModel;
import com.sfmand.des.model.tandem.BlockingTandemModel;
import com.sfmand.des.model.tandem.TandemModel;
import com.sfmand.des.random.DeterministicSequence;
import com.sfmand.des.random.ExponentialVariate;

public class Main {

    private static final long DEFAULT_CUSTOMERS = 100_000;

    public static void main(String[] args) {
        String scenario = args.length > 0 ? args[0] : "all";
        long customers = args.length > 1 ? Long.parseLong(args[1]) : DEFAULT_CUSTOMERS;

        switch (scenario) {
            case "task1-trace" -> task1Trace();
            case "task1-big" -> task1Big(customers);
            case "task2-trace" -> task2Trace();
            case "task2-big" -> task2Big();
            case "task3-trace" -> task3Trace();
            case "task3-big" -> task3Big(customers);
            case "task4-trace" -> task4Trace();
            case "task4-random" -> task4Random();
            case "task4-big" -> task4Big(customers);
            case "task5-trace" -> task5Trace();
            case "task5-big" -> task5Big(customers);
            case "all" -> {
                task1Trace();
                task1Big(customers);
                task2Trace();
                task2Big();
                task3Trace();
                task3Big(customers);
                task4Trace();
                task4Random();
                task4Big(customers);
                task5Trace();
                task5Big(customers);
            }
            default -> {
                System.err.println("Unknown scenario: " + scenario);
                System.exit(1);
            }
        }
    }

    static void task1Trace() {
        Simulation.TRACE_MODE = true;
        SingleServerModel model = new SingleServerModel(
                new DeterministicSequence(8, 6, 1, 8),
                new DeterministicSequence(4, 1, 4, 3),
                4);
        new Simulation(model, TerminationCondition.never()).run().print();
        System.out.println();
    }

    static void task1Big(long m) {
        Simulation.TRACE_MODE = false;
        double meanInterarrival = 10;
        double meanService = 8;
        SingleServerModel model = new SingleServerModel(
                new ExponentialVariate(meanInterarrival, 42),
                new ExponentialVariate(meanService, 7));
        Report report = new Simulation(model, model.untilServiceStarts(m)).run();
        report.print();

        double lambda = report.get("arrivals") / report.get("clock");
        System.out.println("  Little's Law: lambda=" + Numbers.format(lambda, 4)
                + ", lambda*W=" + Numbers.format(lambda * report.get("W"), 4)
                + " vs n_bar=" + Numbers.format(report.get("n_bar"), 4));

        double rho = meanService / meanInterarrival;
        double systemTime = meanService / (1 - rho);
        System.out.println("  Analytical M/M/1: U=" + Numbers.format(rho, 4)
                + ", n_bar=" + Numbers.format(rho / (1 - rho), 4)
                + ", W=" + Numbers.format(systemTime, 4)
                + ", w=" + Numbers.format(systemTime - meanService, 4));
        System.out.println();
    }

    static void task2Trace() {
        Simulation.TRACE_MODE = true;
        FinitePopulationModel model = new FinitePopulationModel(2,
                new DeterministicSequence(1, 2, 4, 5, 6),
                new DeterministicSequence(3, 2, 1),
                10);
        new Simulation(model, TerminationCondition.never()).run().print();
        System.out.println();
    }

    static void task2Big() {
        Simulation.TRACE_MODE = false;
        int terminals = 5;
        double meanThink = 20;
        double meanService = 4;
        FinitePopulationModel model = new FinitePopulationModel(terminals,
                new ExponentialVariate(meanThink, 42),
                new ExponentialVariate(meanService, 7),
                1_000_000);
        new Simulation(model, TerminationCondition.never()).run().print();

        FinitePopulationAnalytical analytical = FinitePopulationAnalytical.of(terminals, meanThink, meanService);
        System.out.println("  Analytical M/M/1//N: U=" + Numbers.format(analytical.u(), 4)
                + ", n_bar=" + Numbers.format(analytical.nBar(), 4)
                + ", W=" + Numbers.format(analytical.bigW(), 4)
                + ", w=" + Numbers.format(analytical.w(), 4));
        System.out.println();
    }
    
    static void task3Trace() {
        Simulation.TRACE_MODE = true;
        TandemModel model = new TandemModel(
                new DeterministicSequence(2, 2),
                List.of(new DeterministicSequence(3, 3, 3), new DeterministicSequence(1, 4, 1)),
                3);
        new Simulation(model, TerminationCondition.never()).run().print();
        System.out.println();
    }

    static void task3Big(long m) {
        Simulation.TRACE_MODE = false;
        double meanInterarrival = 10;
        double[] meanService = {6, 7, 8};
        TandemModel model = new TandemModel(
                new ExponentialVariate(meanInterarrival, 42),
                List.of(new ExponentialVariate(meanService[0], 7),
                        new ExponentialVariate(meanService[1], 11),
                        new ExponentialVariate(meanService[2], 13)));
        Report report = new Simulation(model, TerminationCondition.maxCompletions(m)).run();
        report.print();

        double nBar = 0;
        double w = 0;
        for (double s : meanService) {
            double rho = s / meanInterarrival;
            nBar += rho / (1 - rho);
            w += s / (1 - rho);
        }
        System.out.println("  Analytical (M/M/1 per stage): n_bar=" + Numbers.format(nBar, 4)
                + ", W=" + Numbers.format(w, 4));
        System.out.println();
    }

    static void task4Trace() {
        Simulation.TRACE_MODE = true;
        BlockingTandemModel model = new BlockingTandemModel(
                new DeterministicSequence(1, 1),
                new DeterministicSequence(1, 1, 1),
                new DeterministicSequence(5, 1, 1),
                1, 3);
        new Simulation(model, TerminationCondition.never()).run().print();
        System.out.println();
    }

    static void task4Random() {
        Simulation.TRACE_MODE = true;
        BlockingTandemModel model = new BlockingTandemModel(
                new ExponentialVariate(4, 42),
                new ExponentialVariate(3, 7),
                new ExponentialVariate(6, 11),
                2);
        new Simulation(model, TerminationCondition.maxEvents(40)).run().print();
        System.out.println();
    }

    static void task5Trace() {
        Simulation.TRACE_MODE = true;
        ClosedNetworkModel model = new ClosedNetworkModel(2,
                List.of(() -> 2, () -> 3, () -> 4, () -> 5),
                new DeterministicSequence(0.1, 0.6, 0.9, 0.1, 0.6, 0.9), 12);
        new Simulation(model, TerminationCondition.never()).run().print();
        System.out.println();
    }

    static void task5Big(long visits) {
        if (visits < 1) {
            throw new IllegalArgumentException("Completed visits must be at least 1");
        }
        Simulation.TRACE_MODE = false;
        ClosedNetworkModel model = new ClosedNetworkModel(10,
                List.of(new ExponentialVariate(2, 7), new ExponentialVariate(3, 11),
                        new ExponentialVariate(4, 13), new ExponentialVariate(5, 17)),
                42, 1_000_000);
        new Simulation(model, TerminationCondition.maxCompletions(visits)).run().print();
        System.out.println();
    }

    static void task4Big(long m) {
        Simulation.TRACE_MODE = false;
        BlockingTandemModel model = new BlockingTandemModel(
                new ExponentialVariate(10, 42),
                new ExponentialVariate(6, 7),
                new ExponentialVariate(8, 11),
                2);
        new Simulation(model, TerminationCondition.maxCompletions(m)).run().print();
        System.out.println();
    }
}
