The **Scenario** subsection is where you define and manage the reusable configurations used for REST-based testing in Heimdall. A scenario describes **what to test**, **how to test it**, and **under what conditions**, serving as the blueprint for both scheduled and on-demand executions.

Each scenario can include:

* **Scenario Name & Description** – A meaningful label and optional notes to identify the purpose of the test.
* **Test Tool** – Choice between **JMeter** and **K6**, depending on the preferred execution engine.

Scenarios are designed to be **reusable**. Once defined, they can be run directly as a **simulation** or
linked to the **Schedule** section for automated execution.

From the Scenario dashboard, users can:

* **Create New Scenarios** from scratch.
* **Edit Existing Scenarios** to adjust parameters.
* **Organize Scenarios** into categories or tags for easier retrieval.
* **Validate Scenarios** before execution to ensure correct configuration and prevent test failures caused by
setup issues.

By maintaining a library of scenarios, teams can ensure **consistency in testing**, quickly re-run known workloads
after code changes, and maintain a reliable performance baseline for REST services.
