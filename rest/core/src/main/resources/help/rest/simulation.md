The **Simulation** dashboard displays **aggregated performance metrics** for all recorded runs of a specific registered simulation. A simulation in Heimdall represents a complete load-testing script — usually executed against REST APIs — that may contain one or more scenarios.

This dashboard allows you to track how a simulation has behaved over time, identify trends, and compare results from different runs.

**Typical aggregated metrics include:**

* **Total Runs** – Number of times the simulation has been executed.
* **Average Response Times** – Mean and percentile values (P50/P90/P95/P99) calculated across all runs.
* **Error Rates** – Aggregate percentage of failed requests, with breakdown by error type or HTTP status code.
* **Throughput** – Average and peak requests per second (RPS) over the simulation’s lifetime.
* **Pass/Fail Trends** – Percentage of runs meeting predefined thresholds or SLA criteria.
* **Resource Metrics (if available)** – CPU/memory usage trends for the target service during simulations.

The Simulation dashboard is useful for:

* **Historical performance tracking** – Determine if API performance is improving or degrading.
* **Reliability monitoring** – Identify recurring failures across multiple runs.
* **Capacity validation** – Compare throughput trends across different load levels.

Selecting a simulation from this view allows you to drill down into its **Scenarios** for more granular analysis.