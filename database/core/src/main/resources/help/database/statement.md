The **Statements** dashboard offers a detailed breakdown of the SQL statements executed against the databases during test runs or live monitoring. It gives visibility into query patterns, frequency, and performance characteristics, helping engineers detect inefficiencies, regressions, or unexpected behavior in data access.

This dashboard is especially valuable during performance testing and troubleshooting, where slow queries or excessive repetition can significantly impact application response times or database load.

Each statement entry includes:

* **Statement Text:** The SQL query or command, normalized to group similar queries (e.g., parameterized `SELECT * FROM orders WHERE id = ?`).
* **Execution Count:** How many times the statement has been executed over the monitoring window.
* **Latency Metrics:**
    * **Average Execution Time:** Typical response time across all runs.
    * **Minimum/Maximum Latency:** Observed extremes that may indicate intermittent slowdowns.
* **Database :** Shows where the query was executed, which helps correlate with load or resource issues.
* **Calling User or Application:** Indicates which database user or service originated the statement.
* **Rows Affected or Returned:** Useful for assessing query efficiency (e.g., detecting full table scans or unexpectedly large results).
* **Last Seen Timestamp:** When the statement was last observed in the system, helping identify recently introduced or disappearing queries.

This dashboard is critical for validating SQL performance, spotting unoptimized queries, ensuring caching or indexing strategies are working as intended, and verifying that application code interacts with the database as expected.