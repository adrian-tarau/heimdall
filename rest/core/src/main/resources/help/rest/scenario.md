The **Scenario** subsection provides **aggregated performance metrics for a single scenario** within a simulation. Since a simulation may contain multiple scenarios (e.g., different API endpoints or workflows), this view lets you isolate results for a specific scenario and understand its individual performance.

**Typical aggregated metrics include:**

* **Average & Percentile Latencies** – Scenario-specific P50/P90/P95/P99 values.
* **Error Breakdown** – Distribution of error types or HTTP status codes for this scenario.
* **Throughput** – Requests per second for the scenario, averaged across all simulation runs.
* **Success Rate** – Percentage of successful requests.
* **Trend Analysis** – Historical graph of latency and error rate changes for this scenario.
* **Execution Frequency** – How often this scenario has been run as part of its parent simulation.

The Scenario dashboard is valuable for:

* **Bottleneck isolation** – Pinpointing which part of a test script is slowing down or failing.
* **Targeted troubleshooting** – Focusing investigation on a single workflow or API call.
* **Performance optimization** – Tracking the impact of changes to a specific scenario.