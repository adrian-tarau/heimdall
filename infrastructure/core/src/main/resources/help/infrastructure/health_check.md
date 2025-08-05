The **Health Checks** dashboard in Heimdall provides real-time visibility into the operational status of services through automated and continuous health checks. These metrics are aggregated and used to provide metrics for the **Services** dashboard. The **Health Checks** dashboard dives deeper—showing whether those services are *actually responsive and behaving correctly* at multiple protocol layers.

This dashboard is particularly valuable for QA and software engineers who need to verify not just service availability, but also responsiveness and behavior under load or failure conditions.

Each health check entry provides detailed insights into the following:

* **Name:** Identifies the specific service _instance_.
* **Server and Type:** Indicates the exact server hosting the service and the type of service (Ping, HTTP(s), etc), helping users correlate issues with the underlying infrastructure.
* **Status:** The current health state of the service, determined through checks that span from Layer 3 (network reachability) to Layer 7 (application-level behavior). Examples include:
    * **L3 (Network):** Is the host reachable?
    * **L4 (Transport):** Is the port accepting connections?
    * **L7 (Application):** Is the service returning expected responses (HTTP, gRPC, etc.)?
* **Timing Metrics:** Performance statistics based on recent health checks, including:
    * **Last Check Time:** When the most recent health check was performed.
    * **Average Response Time:** Typical time taken for a successful response.
    * **Minimum/Maximum Response Time:** Extremes observed during the monitoring window.
* **Trend Indicator:** A visual cue showing whether the service health is stable, improving, or deteriorating based on historical checks. This trend helps identify emerging problems before they become critical.

This dashboard is essential for proactively identifying unstable services, diagnosing intermittent failures, and validating that both real and mocked services behave within expected thresholds over time.