The **Databases** section in Heimdall provides deep visibility into the state and behavior of the databases used by applications under test. While Heimdall focuses primarily on mocking external services and monitoring service health, it also tracks the performance and resource utilization of the *real* databases connected to those services.

This section helps QA and software engineers observe how applications interact with their backend data stores under various test conditions. It’s particularly useful for detecting database-related performance bottlenecks, inefficient queries, or transaction anomalies that could impact application behavior in production.

Heimdall collects real-time and historical metrics about:

* **Snapshots** of key metrics for high-level trends and performance regression detection.
* **Nodes** participating in the database cluster, including usage and health status.
* **Users** accessing the databases, along with usage patterns and access frequency.
* **Sessions** currently active, helping identify long-running or idle connections.
* **Transactions** underway, including metrics about their duration and status.
* **Statements** executed by the application, with visibility into latency and frequency.

Together, these dashboards allow users to pinpoint performance issues, understand database utilization patterns, and correlate application behavior with backend performance.
