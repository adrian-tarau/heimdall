The **Running** dashboard displays all REST-based test executions currently in progress, regardless of whether they were started manually from the **Simulation** subsection or triggered automatically via the **Schedule** section. This view is designed to give you real-time operational insight into test activity and resource usage.

**Key information shown for each running simulation:**

* **Simulation Name** – Derived from the linked Scenario or given at launch.
* **Execution Source** – Indicates whether it was started manually (**Simulation**) or via a schedule (**Schedule**).
* **Test Tool** – JMeter or K6.
* **Target Environment** – The system under test (e.g., dev, staging, prod).
* **Start Time & Elapsed Time** – When the run began and how long it has been running.
* **Load Statistics** – Current virtual users, request rate (RPS), and active connections.
* **Performance Metrics** – Live average/median response times, P90/P95 latency, and error rate.
* **Status Indicators** – Pass/fail trends based on defined thresholds and assertion results so far.

**From this dashboard you can:**

* **Monitor in real time** – Watch key performance indicators update as the test runs.
* **View partial results** – Get early insight into potential issues before the test completes.
* **Pause or Resume** – Temporarily halt and later continue a running test (if supported by the chosen engine).
* **Stop or Abort** – End the simulation gracefully or immediately, depending on urgency.
* **Open detailed view** – Drill into logs, failure samples, and per-endpoint performance during the run.

This dashboard is particularly useful for:

* **Spotting failures early** – Stopping a test if the service under test becomes unstable.
* **Validating load patterns** – Ensuring the configured load profile is applied as expected.
* **Coordinating with developers** – Providing live feedback while they watch for system impacts.

---

Do you want me to now integrate this **Running Simulations** dashboard description into the **Rest** documentation as an additional subsection, or keep it as a separate dashboard section in the help?
