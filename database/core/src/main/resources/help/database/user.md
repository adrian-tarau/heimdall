The **Users** dashboard focuses on database-level users—accounts or identities that connect to the database during application execution or test scenarios. This view helps engineers understand which users are active, what their access patterns look like, and whether any user behavior is contributing to performance or stability issues.

This is especially useful in multi-tenant environments or complex test setups where different services or components authenticate with distinct credentials. It also helps detect misconfigurations, such as unexpected users accessing production databases from test environments.

Each user entry includes:

* **Username:** The login or account name used to authenticate against the database.
* **Active Sessions:** Number of current database sessions or connections initiated by the user.
* **Query Volume:** Total number of queries run by the user during the observed time period.
* **Query Latency Metrics:** Aggregated statistics on how fast or slow the user’s queries are executing (average, min, max).
* **Transaction Behavior:** Number of transactions started, committed, or rolled back by the user.
* **Errors or Permission Issues:** Summary of failed login attempts, authorization errors, or query failures tied to the user.

This dashboard supports auditing user activity, optimizing user-specific access patterns, and confirming whether specific credentials are overloading the system or operating outside their expected scope.
