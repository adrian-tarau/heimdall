The **Simulation** subsection lets you run REST tests on demand using **JMeter** or **K6**, without waiting for a
schedule. It’s ideal for quick validation after a code change, reproducing issues seen in production, or
stress-testing a specific endpoint with a controlled load.

**What you can configure before launch**

* **Scenario selection**: Choose a saved Scenario as the base, or start with an ad‑hoc configuration.
* **Execution engine**: JMeter or K6.
* **Target environment**: Pick the real environment (e.g., dev, staging, prod) the traffic will hit.
* **Load profile**:

  * **Virtual users** (VUs/threads)
  * **Steady duration**
* **Data & variables**: CSV/JSON feeders, environment variables, per-run overrides, random seed for reproducibility.
* **Assertions & thresholds**: status codes, latency percentiles (P50/P90/P95/P99), error rate/SLO targets.
* **Limits & safety rails**: max duration, max request rate, abort on threshold breach, concurrency caps.

**What you see during a live run**

* **Live KPIs**: requests/sec, active VUs, error rate, avg/median latency, P90/P95/P99.
* **Code distribution**: 2xx/4xx/5xx breakdown with top failing endpoints.
* **Resource snapshots** (when integrated): service CPU/mem, pod/container restarts, dependency errors.
* **Logs & events**: assertion failures, timeouts, connection errors, and engine logs.
* **Run controls**: pause (if supported), ramp adjustments, **Stop** (graceful) and **Abort** (immediate).

**After the run finishes**

* **Result summary**: pass/fail against thresholds, peak RPS, latency percentiles, failure hotspots.
* **Artifacts**:

  * JMeter: JTL results, sampler/error logs.
  * K6: JSON/CSV summary, trend data.
  * Optional HAR samples and request/response excerpts (redacted).
* **Drill-down views**: slowest endpoints, error traces, correlation with infrastructure metrics.
* **Re-run options**: clone with tweaks (load, env, data seed), promote to **Schedule**, or save as a new **Scenario**.
* **Audit trail**: who ran it, when, engine version, git/tag of the scenario (if linked), parameter overrides.

**Typical uses**

* Verify a fix against a specific failing endpoint.
* Capture a short HAR + traces in debug mode to hand to developers.
* Run a controlled spike to confirm autoscaling or rate-limit behavior.