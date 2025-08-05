The **Services** dashboard provides a focused view of all services running across the monitored infrastructure.

This dashboard helps QA and software engineers understand which services are currently active, where they are hosted, and how they are behaving in their respective environments. It is especially useful when tracking test scenarios that involve complex service interactions, or when validating how applications handle dependency failures and timeouts.

Each service entry includes:

* **Name and Type:** The name of the service, along with an indicator specifying whether it is a *real* monitored service or a *mocked* (fake) service provided by Heimdall.
* **Status:** Displays whether the service is running, degraded, unresponsive, or in an error state. For mocked services, Heimdall may also show the expected behavior (e.g., delayed response, error injection).
* **Uptime and Activity Metrics:** For real services, uptime and usage statistics provide insight into stability and recent activity. For fake services, Heimdall may expose mock interaction logs or trigger configuration summaries.
* **Dependency View:** In some setups, Heimdall may allow viewing or tracing upstream/downstream relationships between services, especially useful in debugging distributed applications.

This dashboard is essential when validating service orchestration, checking behavior in response to mock failures, and ensuring the right set of services is active in a test or QA scenario.