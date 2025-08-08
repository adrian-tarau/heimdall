The **Event** view provides a detailed, time-ordered record of message-related activities within the broker.
Each entry in this view represents a discrete broker event, such as a message being published or delivered.
This is the primary place to inspect the exact flow of messages and confirm whether the system is behaving as
expected.

In Heimdall, the Event dashboard offers:

* **Event Stream** – A chronological list of broker events, including message publication and consumption.
* **Timestamps** – High-resolution timing for when each event occurred, enabling precise latency and sequencing analysis.
* **Message Details** – View payload metadata.

**Typical use cases:**

* **Sequence validation** – Ensure that messages are processed in the correct order during functional and load testing.

By using the Event dashboard, engineers can perform deep diagnostics on broker traffic and gain a ground-level
understanding of message flows, far beyond what aggregate metrics alone can provide.