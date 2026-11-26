package com.sfmand.des;

import com.sfmand.des.engine.Numbers;
import com.sfmand.des.engine.Report;
import com.sfmand.des.engine.Simulation;
import com.sfmand.des.engine.TerminationCondition;
import com.sfmand.des.model.finitepopulation.FinitePopulationAnalytical;
import com.sfmand.des.model.finitepopulation.FinitePopulationModel;
import com.sfmand.des.model.singleserver.SingleServerModel;
import com.sfmand.des.random.DeterministicSequence;
import com.sfmand.des.random.ExponentialVariate;

import java.util.Arrays;

public class Main {

        public static void main(String[] args) {
                boolean traceRandom = Arrays.asList(args).contains("--trace");

                // deterministic data trace
                Simulation.TRACE_MODE = true;
                SingleServerModel testMM1 = new SingleServerModel(
                                new DeterministicSequence(8, 6, 1, 8),
                                new DeterministicSequence(4, 1, 4, 3),
                                4);
                new Simulation(testMM1, TerminationCondition.never()).run().print();
                System.out.println();

                // M/M/1: mean inter-arrival 10, mean service 8 (lambda = 0.8).
                Simulation.TRACE_MODE = traceRandom;
                double meanInterarrival = 10;
                double meanService = 2;
                long customers = 100_000;
                SingleServerModel mm1 = new SingleServerModel(
                                new ExponentialVariate(meanInterarrival, 42),
                                new ExponentialVariate(meanService, 7));
                Report report = new Simulation(mm1, TerminationCondition.maxCompletions(customers)).run();
                report.print();

                double rho = meanService / meanInterarrival;
                double systemTime = meanService / (1 - rho);
                System.out.println("  Analytical M/M/1: U=" + Numbers.format(rho, 4)
                                + ", n_bar=" + Numbers.format(rho / (1 - rho), 4)
                                + ", W=" + Numbers.format(systemTime, 4)
                                + ", w=" + Numbers.format(systemTime - meanService, 4));
                System.out.println();

                // Task 2 deterministic data trace: 2 terminals, term_sim = 10.
                Simulation.TRACE_MODE = true;
                FinitePopulationModel testFinite = new FinitePopulationModel(2,
                                new DeterministicSequence(1, 2, 4, 5, 6),
                                new DeterministicSequence(3, 2, 1),
                                10);
                new Simulation(testFinite, TerminationCondition.never()).run().print();
                System.out.println();

                // M/M/1//N: 5 terminals, mean think 20, mean service 4.
                Simulation.TRACE_MODE = traceRandom;
                int terminals = 5;
                double meanThink = 20;
                double meanFiniteService = 4;
                FinitePopulationModel finite = new FinitePopulationModel(terminals,
                                new ExponentialVariate(meanThink, 42),
                                new ExponentialVariate(meanFiniteService, 7),
                                1_000_000);
                new Simulation(finite, TerminationCondition.never()).run().print();

                FinitePopulationAnalytical analytical = FinitePopulationAnalytical.of(terminals, meanThink,
                                meanFiniteService);
                System.out.println("  Analytical M/M/1//N: U=" + Numbers.format(analytical.u(), 4)
                                + ", n_bar=" + Numbers.format(analytical.nBar(), 4)
                                + ", W=" + Numbers.format(analytical.bigW(), 4)
                                + ", w=" + Numbers.format(analytical.w(), 4));
        }
}
