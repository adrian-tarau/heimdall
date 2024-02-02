## Introduction

_Heimdall_ is a monitoring/testing tool for developers, used to validate or troubleshoot services. The tool provides fake services which are used by applications to mimic a real interaction with a remote service.

However, all services provided by _Heimdall_ will be partially or completely faked. For example, the _SMPT_ server will act like a real SMTP server, but it will not forward the email to real accounts - it will accept an email, store it, make it available for introspection but it will not be forwarded to users.

The following services are faked and available to be used:

* [SMTP](protocol/smtp) (Simple Mail Transfer Protocol)
* [SNMP](protocol/snmp) (Simple Network Management Protocol)
* [Syslog](protocol/syslog) (Standard for Message Logging)
* [GELF](protocol/gelf) (Graylog Extended Logging Format)

A [search engine](search) is available to make all the data collected by these mocked services available to users. Each document is tagged with the _owner_ service and events can be sliced by service or custom attributes. 