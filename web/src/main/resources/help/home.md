## Introduction

_Heimdall_ is a monitoring/testing tool for developers, used to validate or troubleshoot services. The tool provides fake services which are used by applications to mimic a real interaction with a remote service.

However, all services provided by _Heimdall_ will be partially or completely faked. For example, the _SMTP_ server will act like a real SMTP server, but it will not forward the email to real accounts - it will accept an email, store it, make it available for introspection, but it will not be forwarded to users.

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

If _Heimdall_ runs in containers, check on which port the above ports (services) are configured. 

### Workspace

The application [workspace](workspace) is organized in a typical web application layout: header, navigation and content area. 

### Information Searching (Querying)

A [search engine](search) is available to make all the data collected by these mocked services available to be searched by users (developers). Each document is tagged with the _owner_ service and events can be sliced by service or custom attributes. 

Outside the _search engine_ dashboards, most dashboards expose a toolbar for various actions and filters and a data grid (or multiple panels with widgets) for [data exploration](dataset/dashboard). 