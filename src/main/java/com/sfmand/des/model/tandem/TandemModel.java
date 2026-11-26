package com.sfmand.des.model.tandem;

import com.sfmand.des.model.network.Station;
import com.sfmand.des.random.Variate;

import java.util.List;

public class TandemModel extends AbstractTandemModel {

    public TandemModel(Variate interarrival, List<? extends Variate> serviceTimes) {
        this(interarrival, serviceTimes, Long.MAX_VALUE);
    }

    public TandemModel(Variate interarrival, List<? extends Variate> serviceTimes, long maxArrivals) {
        super(interarrival, maxArrivals);
        if (serviceTimes.isEmpty()) {
            throw new IllegalArgumentException("A tandem queue needs at least one stage");
        }
        int m = serviceTimes.size();
        for (int i = 0; i < m; i++) {
            int k = i + 1;
            stations.add(new Station("S" + k, "D" + k, "Departure " + k, serviceTimes.get(i), -k));
        }
        for (int i = 0; i < m - 1; i++) {
            Station next = stations.get(i + 1);
            stations.get(i).onServiceEnd((sim, st) -> next.arrive(sim, st.release(sim)));
        }
        stations.get(m - 1).onServiceEnd((sim, st) -> exit(sim, st.release(sim)));
    }

    @Override
    public String name() {
        return "Tandem Queue (" + stations.size() + " stages)";
    }
}