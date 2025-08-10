All services provided by _Heimdall_ will be partially or completely faked. For example, the _SMTP_ server will act like a real SMTP server, but it will not forward the email to real accounts - it will accept an email, store it, make it available for introspection, but it will not be forwarded to users.

The following services are faked and available to be used:

* [SMTP](protocol/smtp) (Simple Mail Transfer Protocol)
    * Plain `2525`: No authentication
    * TLS `2526`: No authentication
* [SNMP](protocol/snmp) (Simple Network Management Protocol)
    * UDP `2161`: V2
    * UDP `2162`: V3
* [Syslog](protocol/syslog) (Standard for Message Logging)
    * UDP `2514`:
    * TCP `2601`:
* [GELF](protocol/gelf) (Graylog Extended Logging Format)
    * UDP `12201`:
    * TCP `12200`:

If _Heimdall_ runs in containers, check on which port the above  services are configured (what is available to clients).

The events/alerts sent to the above services can be inspected under dashboards available under _Protocols_ section of the _Heimdall_. 