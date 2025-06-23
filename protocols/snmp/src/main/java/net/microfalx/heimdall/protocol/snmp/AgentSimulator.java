package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.heimdall.protocol.snmp.mib.MibService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AgentSimulator implements InitializingBean {

    @Autowired(required = false)
    private SnmpProperties properties = new SnmpProperties();

    @Autowired
    private AgentServer agentServer;

    @Autowired
    private SnmpService snmpService;

    @Autowired
    private MibService mibService;

    @Override
    public void afterPropertiesSet() throws Exception {
        
    }
}
