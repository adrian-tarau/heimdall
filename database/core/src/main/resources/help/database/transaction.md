The **Transactions** dashboard provides real-time and historical insight into database transactions—units of work that involve one or more queries, often encapsulated between `BEGIN` and `COMMIT` (or `ROLLBACK`) statements. This dashboard is critical for understanding how applications interact with the database under test, particularly in terms of concurrency, contention, and failure behavior.

QA and software engineers can use this view to trace transactional behavior across services, identify slow or stuck transactions, and validate how rollback scenarios behave under fault injection or load.

Each transaction entry includes:

* **ID:** A unique identifier for the transaction, as assigned by the database engine.
* **Session and User:** Links the transaction to the session and user that initiated it, providing traceability back to the client or service.
* **Node:** Indicates where the transaction is executing, to help correlate with infrastructure or cluster-level behavior.
* **State:** The current status of the transaction:
    * *Active* — queries are still being executed
    * *Idle* — awaiting further commands
    * *Committing/Rolling back* — finalizing or undoing
    * *Blocked* — waiting for a lock or resource
* **Start Time and Duration:** When the transaction started and how long it has been running. Long-running transactions may indicate uncommitted changes or application issues.
* **Locks Held or Contended:** Shows any table/row-level locks held or requested, and whether the transaction is blocking or being blocked by others.
* **Active Statement:** The active statement, running under the transaction, if any..
* **Statement Count:** Number of SQL statements executed within the transaction, offering a glimpse into its complexity.

This dashboard is essential when verifying transaction isolation behavior, diagnosing deadlocks or contention issues, and confirming whether applications are managing transactions efficiently under load.
