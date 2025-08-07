The **Nodes** dashboard provides visibility into the individual database servers (or instances) that make up a distributed or clustered database deployment. This view is particularly useful when monitoring horizontally scaled databases (e.g., sharded PostgreSQL, distributed SQL databases, or NoSQL clusters), where performance issues may stem from imbalances or failures on specific nodes.

This subsection enables engineers to assess both the health and workload distribution across nodes in real time or historically, which is essential during high-load testing or when validating failover and replication behaviors.

Each node entry includes the following key details:

* **Name:** A unique name or ID for the node, typically mapped to a physical or virtual host in the cluster.
* **Health Status:** Real-time node health, highlighting connectivity issues, replication lag, or degraded performance.
* **Resource Usage:** CPU, memory, and disk utilization at the node level—helpful for identifying overloaded or underused nodes.
* **Connections:** Active client or service connections to the node, including any imbalances or spikes.
* **Replication Metrics:** For systems using replication, this includes role (primary/replica), replication lag, and catch-up status.
* **Error and Warning Logs:** If available, recent alerts or errors surfaced at the node level.

This dashboard helps QA and software engineers validate whether tests are affecting all nodes equally, detect skewed load distribution, and confirm that failover or scale-out mechanisms behave as expected.
