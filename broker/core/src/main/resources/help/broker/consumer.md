The **Consumer** view focuses on the applications, services, or components that retrieve messages from the broker.
In distributed systems, consumers are critical for processing data and triggering downstream actions.
Monitoring their activity ensures that the right messages are received in the correct order, without delays or loss.

In Heimdall, the Consumer dashboard provides:

* **Consumer List** – Displays all active and recently active consumers, along with identifiers such as consumer name,
subscription/topic.
* **Status Indicators** – Shows whether each consumer is currently connected, idle, or disconnected.
* **Message Throughput** – Tracks the number of messages consumed over time, helping to detect slowdowns or sudden spikes in traffic.

**Typical use cases:**

* **Validation testing** – Confirm that test consumers are receiving all messages published by the producer in a mock service scenario.
* **Load analysis** – Observe how consumer throughput changes under different test conditions.

By monitoring the Consumer dashboard, you can quickly identify whether an issue lies in the producer, the broker, or
the consumer itself, streamlining root-cause analysis.