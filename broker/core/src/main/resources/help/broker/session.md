The **Session** view focuses on the lifecycle of connections between clients (producers or consumers) and the broker.
A session represents a continuous interaction period, starting when a client connects to the broker and ending when it
disconnects. Monitoring session activity is essential for understanding connection stability, diagnosing drops,
and correlating issues with specific time periods.

In Heimdall, the Session dashboard provides:

* **Active Sessions** – Displays currently connected producers and consumers, along with their connection duration and activity level.
* **Historical Sessions** – Lists recently ended sessions, with timestamps for when connections were established and closed.
* **Connection Metadata** – Includes authentication status and associated topics/queues.
* **Activity Metrics** – Shows how many messages were sent or received during each session, helping to identify idle or overly busy connections.

**Typical use cases:**

* **Connection stability monitoring** – Detect frequent reconnects or unexpected disconnects that may indicate network or configuration issues.
* **Test environment correlation** – Match specific test runs to the sessions in which they occurred, aiding in reproducibility of results.
* **Capacity analysis** – Review how many concurrent sessions the broker is handling and how evenly load is distributed.

By tracking session activity, engineers can better understand the operational patterns of their messaging environment and quickly identify anomalies in connection behavior.
