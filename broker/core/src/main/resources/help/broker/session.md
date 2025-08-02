In a Broker Session, the consumer connects with a broker to retrieve messages and their metadata from a specific
message topic. During this session, all session data, including messages and their associated metadata, is stored
in a database. The events are collected and saved in a snapshot. The session is deemed complete
when the snapshot no longer contains any events.

The session concludes when there are no more events available to poll from the topic, or when the maximum number
of events has been reached. This clear condition for ending the session ensures that the process is not prolonged
unnecessarily.
