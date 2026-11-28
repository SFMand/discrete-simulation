# Task 5: Closed Network

Requires Java 21 and Maven. This branch builds on `task3-task4` and reuses its
`Station`, `Job`, and service-end events. Merge Tasks 3/4 before integrating this
branch into `master`.

```sh
mvn test
mvn package
java -jar target/discrete-simulation-0.1.0-SNAPSHOT.jar task5-trace
java -jar target/discrete-simulation-0.1.0-SNAPSHOT.jar task5-big 200000
```

`task5-trace` enables `Simulation.TRACE_MODE` and runs a reproducible two-job
example until time 12. `task5-big` disables trace and uses 10 circulating jobs,
exponential mean service times 2, 3, 4, and 5, and routing seed 42. Its optional
argument is the target number of **completed server visits**, not the population.
It stops at that count or the time limit 1,000,000, whichever comes first.
These service times and stopping limits are demonstration settings; Task 5 does
not prescribe them. The `all` scenario also includes both Task 5 demonstrations.

All N jobs start at Server 1 at time zero (one in service, N-1 waiting). A uniform
draw from [0,1) routes Server 1 departures to Server 2 below 0.5, Server 3 from
0.5 up to 0.8, and Server 4 otherwise. Each peripheral server returns the same
job to Server 1. No external arrivals or permanent departures are scheduled.
The total population remains N after every event.

The model reports `w_k`, `n_bar_k`, and `U_k` for each server. Waiting times use
completed visits only, excluding the wait of a visit still in service when the
simulation stops. Overall `w` is the mean queue wait per completed server visit;
overall `n_bar` must equal N. `completions` counts all server visits, while
`cycles` counts returns to Server 1. Observed routing fractions should approach
0.5, 0.3, and 0.2 in long runs. Utilization is reported separately for each server.

For the deterministic example, service times are [2,3,4,5], and the first four
routing draws are [0.1,0.6,0.9,0.1]. Departures occur at times 2,4,5,7,8,10,12,
followed by the stop event at 12. The hand-calculated results are:

| Metric | Expected value |
| --- | --- |
| Population / overall n_bar | 2 |
| Completed visits / returns to Server 1 | 7 / 3 |
| Total area | 24 |
| Station areas S1..S4 | 10, 5, 4, 5 |
| Active times S1..S4 | 8, 5, 4, 5 |
| Overall mean queue wait | 2/7 |
| Routes to S2, S3, S4 | 2, 1, 1 |

Tests cover initialization, deterministic trace and metrics, routing thresholds,
all return paths, unfinished visits at termination, population conservation and
absence of external arrival events over 200,000 visits, routing proportions,
and invalid parameters. Service durations must be finite and positive.
