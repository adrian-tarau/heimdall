[Simple Network Management Protocol (SNMP)](https://en.wikipedia.org/wiki/Simple_Network_Management_Protocol) is an internet standard protocol used to monitor and manage network devices connected over an IP. SNMP is used for communication between routers, switches, firewalls, load balancers, servers, CCTV cameras, and wireless devices.

_Heimdall_ acts like such a SNMP server, receives the network event and stores it for later retrieval and exploration.

Each SNMP trap received by _Heimdall_ will be displayed in the dashboard (by default sorted by received time in descending order) and the whole trap can be visualized by clicking on the _Message_ column: 

In the absence of any configuration for a given MIB, _Heimdall_ creates a message out of a few bind variables which seem to have textual content. However, it is recommended to configure for each MIB which bind variable holds the trap message. 

If the _Variabes_ tab is selected, the view displays all bind variables from the current trap:

