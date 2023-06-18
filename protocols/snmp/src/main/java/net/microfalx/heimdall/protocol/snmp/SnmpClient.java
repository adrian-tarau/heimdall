package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.heimdall.protocol.core.ProtocolClient;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.net.ProtocolException;

import static org.snmp4j.mp.SnmpConstants.SNMP_ERROR_SUCCESS;

public class SnmpClient extends ProtocolClient<SnmpTrap> {

    private static final long startTime = System.currentTimeMillis();
    private int version = SnmpConstants.version2c;

    @Override
    protected int getDefaultPort() {
        return 162;
    }

    /**
     * Returns the SNMP version to be used.
     *
     * @return the version
     * @see SnmpConstants#version1
     * @see SnmpConstants#version2c
     * @see SnmpConstants#version3
     */
    public int getVersion() {
        return version;
    }

    /**
     * Changes the SNMP version to be sued
     *
     * @param version the version
     * @see #getVersion()
     */
    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    protected void doSend(SnmpTrap event) throws IOException {
        PDU pdu;
        if (version == SnmpConstants.version3) {
            ScopedPDU scopedPDU = new ScopedPDU();
            scopedPDU.setContextEngineID(new OctetString("heimdall"));
            scopedPDU.setContextName(new OctetString("Heimdall"));
            pdu = scopedPDU;
        } else {
            pdu = new PDUv1();
        }
        pdu.setType(PDU.TRAP);
        pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(getSysUpTime())));
        pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, SnmpConstants.linkDown));

        Target<IpAddress> target;
        if (version == SnmpConstants.version3) {
            UserTarget<IpAddress> userTarget = new UserTarget<>();
            userTarget.setAddress(getSnmpAddress());
            userTarget.setRetries(3);
            userTarget.setTimeout(500);
            userTarget.setVersion(SnmpConstants.version3);
            userTarget.setSecurityLevel(SecurityLevel.AUTH_PRIV);
            userTarget.setSecurityName(new OctetString("MD5DES"));
            target = userTarget;
        } else {
            CommunityTarget<IpAddress> communityTarget = new CommunityTarget<>();
            communityTarget.setCommunity(new OctetString("public"));
            communityTarget.setAddress(getSnmpAddress());
            communityTarget.setVersion(SnmpConstants.version2c);
            target = communityTarget;
        }

        Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
        ResponseEvent<IpAddress> response = snmp.send(pdu, target);
        if (response != null && response.getResponse() == null && response.getResponse().getErrorStatus() != SNMP_ERROR_SUCCESS) {
            throw new ProtocolException("Failed to send SNMP trap to " + getHostName() + ", reason: " + response.getResponse().getErrorStatusText());
        }
    }

    private IpAddress getSnmpAddress() {
        if (getTransport() == Transport.UDP) {
            return new UdpAddress(getAddress(), getPort());
        } else {
            return new TcpAddress(getAddress(), getPort());
        }
    }

    private int getSysUpTime() {
        return (int) ((System.currentTimeMillis() - startTime) / 10);
    }
}
