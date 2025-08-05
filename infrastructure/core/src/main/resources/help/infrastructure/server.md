The **Servers**  dashboard provides detailed, individual-level visibility into the physical or virtual servers within each cluster and environment. Servers represent the fundamental infrastructure components that host applications and the services being monitored or mocked by Heimdall.

A server is a machine that provides information to other
machines called clients on computer network.
This architecture is called the client–server model.
Servers can provide various functionalities, often called
[services](admin/infrastructure/service).

A single server can serve multiple clients, and a single
client can use multiple servers. A client process may run
on the same device or may connect over a network to a server
on a different device. Typical servers are database servers,
file servers, mail servers, print servers, web servers,
game servers, and application servers.

Client–server systems are usually most frequently implemented
by (and often identified with) the request–response model:
a client sends a request to the server, which performs some
action and sends a response back to the client, typically
with a result or acknowledgment. Designating a computer as
"server-class hardware" implies that it is specialized for
running servers on it. This often implies that it is more
powerful and reliable than standard personal computers, but
alternatively, large computing clusters may be composed of
many relatively simple, replaceable server components.

In this subsection, users can rapidly assess individual server health, resource utilization, and associated workloads. This granularity simplifies troubleshooting efforts, aids resource planning, and helps ensure optimal infrastructure performance.

Each server listed provides the following information:

* **Name and Description:** Clearly identifies the server, typically including details about its role or physical location (e.g., "API Server 01," "Mock Service Host 03").
* **Status:** Real-time indicators of server health, availability, and any ongoing issues or maintenance activities.
* **Service Counts:** The number of active servers in the server,by status, clearly differentiating between healthy and faulty services.
* **Performance Metrics:** Key resource usage metrics (CPU, memory, disk, network), enabling quick diagnosis of performance issues or resource constraints.
* **Connectivity and Uptime Information:** Current uptime data and basic connectivity metrics help to quickly identify server availability or network-related problems.

This dashboard enables engineers and administrators to have an in-depth understanding of their infrastructure at the individual server level, assisting with precise troubleshooting, accurate resource provisioning, and efficient maintenance planning.