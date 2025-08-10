The **Producer** dashboard tracks the activity of message producers specifically used to test the **stability** and **availability** of the monitored brokers. Unlike application producers that serve business logic, these producers in Heimdall are designed to simulate a **realistic publishing workload** for performance and reliability testing. By doing so, they help generate consistent, measurable traffic that can be analyzed to assess broker health under controlled load.

In Heimdall, the Producer dashboard provides:

* **Producer List** – Displays all test producers currently active or recently active, along with identifiers such as producer name, target broker, and topic/queue.
* **Status Indicators** – Shows whether each producer is actively publishing, idle, or disconnected.
* **Message Throughput** – Tracks the rate of published messages over time, helping measure broker responsiveness and detect anomalies such as throttling or slow acknowledgments.
* **Latency & Acknowledgment Times** – Measures the time from message publish to broker acknowledgment, highlighting any delays or instability.
* **Message Size & Payload Patterns** – Reports the average size of messages being sent to ensure the simulated workload matches realistic traffic.
* **Error Metrics** – Captures failed publish attempts, broker rejections, or timeout occurrences.

**Typical use cases:**

* **Broker performance benchmarking** – Establish baseline metrics for broker throughput and latency under simulated workloads.
* **Stability testing** – Run extended publishing sessions to identify broker degradation over time.
* **Failure detection** – Detect sudden spikes in publish errors or acknowledgment delays that may indicate broker issues.

By monitoring this dashboard, QA and software engineers can validate whether brokers can handle expected workloads reliably and consistently, while also detecting early signs of performance degradation.
