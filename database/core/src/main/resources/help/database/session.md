The **Sessions** dashboard provides a live and historical view of all active database connections, including those initiated by real applications, test harnesses, or Heimdall’s mocked services. Each session represents an open connection to the database, which may span multiple transactions or queries.

This dashboard is especially valuable when debugging connection pool saturation, idle connection buildup, or issues with long-running sessions that could hold locks or consume excessive resources.

Each session entry includes:

* **ID:** A unique identifier for the session, often provided by the database engine.
* **User and Host:** Identifies which user initiated the session and from which server or IP address the connection originated.
* **Node:** Contextual information about the environment and database node handling the session.
* **State:** Whether the session is currently *active*, *idle*, *waiting*, or *terminated*.
* **Start Time and Duration:** When the session was opened, and how long it has been active—useful for spotting stale or forgotten connections.
* **Current Query (if active):** Displays the query currently being executed, if any, for active sessions.
* **Transaction Status:** Indicates whether a transaction is open within the session and whether it's committing, rolling back, or blocked.
* **Locks Held or Waited On:** Details about any locks the session is holding or waiting for, which helps detect blocking situations.

This dashboard is vital for identifying resource leaks, blocked transactions, unbalanced connection usage, and potential issues with client behavior during tests.
