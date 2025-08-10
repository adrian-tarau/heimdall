The **Event** dashboard displays all events received from a topic in **chronological order**, providing a clear, time-based sequence of broker activity. This dashboard focuses on presenting raw event data as it was captured, allowing engineers to trace exactly what was sent and when.

In Heimdall, the Event dashboard includes:

* **Broker & Topic** – Identifies the source broker and the specific topic from which the event was received.
* **Session Reference** – Shows which session extracted the event, making it easy to correlate events with specific capture windows.
* **Event Information**
    * **ID** – The unique identifier of the event.
    * **Name** – Extracted using a template, which is configured to pull attributes via JSON paths from the event payload.
* **Timestamps**
    * **Created** – The original timestamp of the event as recorded by the producing system.
    * **Received** – The timestamp when Heimdall retrieved the event from the topic.

Events are displayed in chronological order based on creation time, allowing easy visual tracking of the event sequence.

**Typical use cases:**

* **Sequence validation** – Confirm that events are arriving in the correct order and without gaps.
* **Correlation with sessions** – Quickly see which controlled capture period produced specific events.
* **Event attribute inspection** – Verify that the correct attributes are being extracted via JSON paths and mapped into event names.

By using the Event dashboard, engineers can precisely trace message activity from creation to capture, making it an essential tool for debugging broker-based workflows.
