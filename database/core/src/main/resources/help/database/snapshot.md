The **Snapshots** dashboard provides a high-level overview of database performance and resource usage over time. Each snapshot captures the state of key metrics at a specific point, allowing users to track trends, compare test runs, and identify regressions or anomalies in backend behavior.

This view is especially helpful for QA engineers conducting performance or stress tests, as it allows them to evaluate how database performance evolves across different application states or test scenarios.

Each snapshot typically includes:

* **Timestamp:** The exact moment the snapshot was taken, often aligned with a specific test execution or environment event.
* **CPU and Memory Usage:** Aggregated metrics showing database resource consumption at the system level.
* **Active Sessions and Transactions:** Counts of current user sessions and active transactions, indicating workload intensity at the time of the snapshot.
* **Query Throughput:** The number of queries executed per second (QPS), which helps in measuring system load.
* **Slow Queries:** Number and percentage of queries exceeding defined latency thresholds, useful for spotting performance degradation.
* **Disk I/O and Storage:** Read/write throughput and free space indicators to identify I/O bottlenecks or capacity issues.
* **Error Rates:** Counts of failed queries or transaction errors captured during the snapshot window.

Snapshots act as a point-in-time performance report card. Comparing snapshots across different times, environments, or test runs can reveal whether recent changes (in code, configuration, or data) are causing performance issues.
