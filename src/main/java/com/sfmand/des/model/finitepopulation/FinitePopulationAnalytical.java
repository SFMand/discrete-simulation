package com.sfmand.des.model.finitepopulation;

/**
 * Steady-state values of the M/M/1//N queue, used to validate simulation output.
 * p_k = p_0 * N!/(N-k)! * (meanService/meanThink)^k
 *
 * @param u    server utilization
 * @param nBar time-average number in system
 * @param w    average waiting time in queue
 * @param bigW average time in system
 */
public record FinitePopulationAnalytical(double u, double nBar, double w, double bigW) {

    public static FinitePopulationAnalytical of(int n, double meanThink, double meanService) {
        double ratio = meanService / meanThink;

        double weight = 1.0;
        double weightSum = 1.0;
        double weightedK = 0.0;
        for (int k = 1; k <= n; k++) {
            weight *= (n - k + 1) * ratio;
            weightSum += weight;
            weightedK += k * weight;
        }

        double u = 1.0 - 1.0 / weightSum;
        double nBar = weightedK / weightSum;
        double lambdaEff = u / meanService;
        double bigW = nBar / lambdaEff;
        return new FinitePopulationAnalytical(u, nBar, bigW - meanService, bigW);
    }
}
