The **Broker** section in Heimdall provides real-time visibility into the flow of messages between applications and services. In many distributed systems, a message broker acts as the intermediary responsible for routing, queuing, and delivering data between producers (which send messages) and consumers (which receive them).
Heimdall’s Broker dashboard allows you to monitor these interactions in detail, helping you validate message delivery, troubleshoot issues, and analyze service behavior under different conditions.

While Heimdall is often used to mimic external services, the Broker dashboard focuses on monitoring
**real message flows** happening in your environment. This includes tracking message events, sessions,
and the activity of both producers and consumers. You can use it to confirm that applications are publishing
 and consuming messages as expected, and to investigate irregularities such as delivery delays, missing
 acknowledgments, or unusual traffic spikes.

The section is organized into four main dashboards:

* **Session** – Tracks ongoing and past broker sessions, including connection details and lifecycle events.
* **Events** – Shows a detailed log of message events, including timestamps, payload details, and delivery outcomes.
* **Consumers** – Displays the state and activity of services or components that consume messages from the broker.
* **Producers** – Displays the state and activity of services or components that publish messages to the broker.


By combining these perspectives, the Broker dashboard offers both a high-level overview of broker health and detailed drill-downs for specific troubleshooting scenarios. It is especially valuable for QA and software engineers
 testing integrations, as it can reveal whether the test environment is sending and receiving the right messages at the right time.