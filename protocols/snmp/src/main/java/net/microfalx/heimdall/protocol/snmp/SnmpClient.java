package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.heimdall.protocol.core.ProtocolClient;
import net.microfalx.heimdall.protocol.core.ProtocolException;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static org.snmp4j.mp.SnmpConstants.SNMP_ERROR_SUCCESS;
import static org.snmp4j.mp.SnmpConstants.sysDescr;

public class SnmpClient extends ProtocolClient<SnmpEvent> {

    public static String DEFAULT_MESSAGE_OID = sysDescr.toDottedString();

    private static final long startTime = System.currentTimeMillis();
    private int version = SnmpConstants.version2c;
    private String messageOid = DEFAULT_MESSAGE_OID;

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

    /**
     * Returns the OID which carries the event message.
     * @return a non-null instance
     */
    public String getMessageOid() {
        return messageOid;
    }

    /**
     * Changes the OID which carries the event message.
     * @param messageOid the OID
     */
    public void setMessageOid(String messageOid) {
        requireNonNull(messageOid);
        this.messageOid = messageOid;
    }

    @Override
    protected void doSend(SnmpEvent event) throws IOException {
        PDU pdu = createPdu(event);
        updateBindings(pdu, event);
        Target<IpAddress> target = updateVersionAndTarget(pdu, event);

        Snmp snmp = new Snmp(getTransport() == Transport.UDP ? new DefaultUdpTransportMapping() : new DefaultTcpTransportMapping());
        ResponseEvent<IpAddress> response = snmp.send(pdu, target);
        if (response != null && response.getResponse() == null && response.getResponse().getErrorStatus() != SNMP_ERROR_SUCCESS) {
            throw new ProtocolException("Failed to send SNMP trap to " + getHostName() + ", reason: " + response.getResponse().getErrorStatusText());
        }
    }

    private void updateBindings(PDU pdu, SnmpEvent event) {
        pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(getSysUpTime())));
        pdu.add(new VariableBinding(new OID(messageOid), new OctetString(event.getBodyAsString())));
        for (Map.Entry<String, Object> entry : event.getAttributes().entrySet()) {
            Object value = entry.getValue();
            Variable variable;
            if (value == null) {
                variable = new Null();
            } else if (value instanceof Integer) {
                variable = new Counter32(((Integer) value));
            } else if (value instanceof Long) {
                variable = new Counter64(((Long) value));
            } else if (value instanceof String) {
                variable = new OctetString(((String) value));
            } else if (value instanceof Variable) {
                variable = (Variable) value;
            } else {
                throw new ProtocolException("Unknown variable bind data type: " + value.getClass());
            }
            pdu.add(new VariableBinding(new OID(entry.getKey()), variable));
        }
    }

    private PDU createPdu(SnmpEvent event) {
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
        return pdu;
    }

    private Target<IpAddress> updateVersionAndTarget(PDU pdu, SnmpEvent event) {
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
        return target;
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
