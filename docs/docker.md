# Docker

Build Heildall container (if needed) with the following command (executed in `deploy` module):

`mvn jib:dockerBuild`

The `compose.yaml` should be changed if needed (passwords, volumes, etc). By default, Heimdall exposes all required ports (web container, protocols, etc) starting with port 9800, but they can be changed as needed. Latest version of the container (the development branch) is used unless it is changed.

The protocols are mapped to the following ports:
* `9805`: SMTP
* `9806`: SMTP with TLS 
* `9807`: SNMP over UDP
* `9808`: SNMP over TCP
* `9809` Syslog over UDP
* `9810`: Syslog over TCP
* `9811`: GELF over UDP
* `9812`: GELF over TCP 

The environment variable `HEIMDALL_PROTOCOL_SIMULATOR_ENABLED` can be set to `true` to enable simulators for protocols.

Run the following command to start up Heimdall:

```bash
docker compose -p heimdall up
```