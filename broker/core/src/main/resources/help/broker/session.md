The **Session** dashboard displays detailed information about a single, bounded consumption session. A session begins when a consumer starts listening to a topic and ends once it reaches any of the defined limits: maximum total event size, maximum event count, or maximum duration. This makes it particularly useful for tracking controlled message captures during testing or diagnostics.

In Heimdall, the Session dashboard includes:
* **Broker & Topic** – Identifies the broker and specific topic from which events were consumed.
* **Event Counts & Sizes** – Shows the total number of events consumed during the session and the average event size, helping to gauge message throughput and payload characteristics.
* **Session Timing** – Displays the start time, end time, and total duration of the session.
* **Status Indicators**
    * **Successful** – The session completed normally and captured events as expected.
    * **Failed** – The session ended due to an error.
    * **Canceled** – The topic had no events during the session interval, and no data was captured.
* **Error Details** – If the session status is *Failed* or *Successful* with errors, the error message is shown for further analysis.

An aggregated view of all events can be viewed by clicking on the session, allowing you to inspect the individual attributes present in the events captured during that session.

**Typical use cases:**

* **Targeted topic monitoring** – Capture a snapshot of activity from a specific topic to analyze payloads and throughput.
* **Intermittent issue tracking** – Schedule bounded sessions to see if issues occur during specific time windows.
* **Payload analysis** – Review event size patterns to identify anomalies or inefficiencies in message formatting.

By using the Session dashboard, engineers can perform focused investigations into broker activity on a per-topic basis, without being overwhelmed by unrelated traffic.
