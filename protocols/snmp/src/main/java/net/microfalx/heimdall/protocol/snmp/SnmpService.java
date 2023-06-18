package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.heimdall.protocol.core.ProtocolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SnmpService extends ProtocolService<SnmpTrap> {

    @Autowired
    private SnmpSimulator simulator;

    @Autowired
    private SnmpProperties properties;

    @Override
    protected SnmpSimulator getSimulator() {
        return simulator;
    }

    public void handle(SnmpTrap trap) {

    }

}
