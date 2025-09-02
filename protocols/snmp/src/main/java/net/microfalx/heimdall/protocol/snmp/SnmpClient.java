package net.microfalx.heimdall.protocol.snmp;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.model.Attribute;
import net.microfalx.heimdall.protocol.core.Event;
import net.microfalx.heimdall.protocol.core.ProtocolClient;
import net.microfalx.heimdall.protocol.core.ProtocolException;
import net.microfalx.metrics.Metrics;
import org.snmp4j.*;
import org.snmp4j.event.CounterEvent;
import org.snmp4j.event.CounterListener;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.CounterSupport;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static org.snmp4j.mp.SnmpConstants.SNMP_ERROR_SUCCESS;
import static org.snmp4j.mp.SnmpConstants.sysDescr;

public class SnmpClient extends ProtocolClient<SnmpEvent> {

    public static String DEFAULT_MESSAGE_OID = sysDescr.toDottedString();

    private static final long startTime = System.currentTimeMillis();

    @Getter private final SnmpMode mode;

    /**
     * The SNMP version to be used.
     */
    @Setter
    @Getter
    private int version = SnmpConstants.version2c;
    @Getter
    private String messageOid = DEFAULT_MESSAGE_OID;
    @Setter
    @Getter
    private int pduType = PDU.GET;
    private Snmp cachedSnmp;
    private final ResponseListener listener = new ResponseListenerImpl();
    private final Metrics OIDS_METRIC;

    public SnmpClient(SnmpMode mode) {
        requireNonNull(mode);
        this.mode = mode;
        OIDS_METRIC = METRICS.withGroup("OID");
    }

    @Override
    protected int getDefaultPort() {
        return mode == SnmpMode.AGENT ? 161 : 162;
    }

    /**
     * Changes the OID which carries the event message.
     *
     * @param messageOid the OID
     */
    public void setMessageOid(String messageOid) {
        requireNonNull(messageOid);
        this.messageOid = messageOid;
    }

    @Override
    protected Event.Type getEventType() {
        return Event.Type.SNMP;
    }

    @Override
    protected void doSend(SnmpEvent event) throws IOException {
        PDU pdu = createPdu(event);
        updateAgentBindings(pdu, event);
        updateTrapBindings(pdu, event);
        Target<IpAddress> target = updateVersionAndTarget(pdu, event);
        Snmp snmp = getSnmp();
        snmp.send(pdu, target, event, listener);
    }

    private void updateAgentBindings(PDU pdu, SnmpEvent event) {
        if (mode == SnmpMode.TRAP) return;
        for (Attribute attribute : event) {
            pdu.add(new VariableBinding(new OID(attribute.getName())));
        }
    }

    private void updateTrapBindings(PDU pdu, SnmpEvent event) {
        if (mode == SnmpMode.AGENT) return;
        pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(getSysUpTime())));
        pdu.add(new VariableBinding(new OID(messageOid), new OctetString(event.getBodyAsString())));
        for (Attribute attribute : event) {
            Object value = attribute.getValue();
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
            pdu.add(new VariableBinding(new OID(attribute.getName()), variable));
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
            pdu = new PDU();
        }
        if (mode == SnmpMode.AGENT) {
            pdu.setType(event.getPduType());
            if (event.getPduType() == PDU.GETBULK) {
                pdu.setType(PDU.GETBULK);
                pdu.setMaxRepetitions(event.getMaxRepetitions());
            }
        } else {
            pdu.setType(PDU.TRAP);
        }
        return pdu;
    }

    private Target<IpAddress> updateVersionAndTarget(PDU pdu, SnmpEvent event) {
        Target<IpAddress> target;
        if (version == SnmpConstants.version3) {
            UserTarget<IpAddress> userTarget = new UserTarget<>();
            userTarget.setVersion(SnmpConstants.version3);
            userTarget.setSecurityLevel(SecurityLevel.AUTH_PRIV);
            userTarget.setSecurityName(new OctetString("MD5DES"));
            target = userTarget;
        } else {
            CommunityTarget<IpAddress> communityTarget = new CommunityTarget<>();
            communityTarget.setAddress(getSnmpAddress());
            communityTarget.setVersion(SnmpConstants.version2c);
            communityTarget.setCommunity(new OctetString("public"));
            target = communityTarget;
        }
        target.setAddress(getSnmpAddress());
        target.setTimeout(5000);
        target.setRetries(3);
        return target;
    }

    private IpAddress getSnmpAddress() {
        if (getTransport() == Transport.UDP) {
            return new UdpAddress(getAddress(), getPort());
        } else {
            return new TcpAddress(getAddress(), getPort());
        }
    }

    private Snmp getSnmp() throws IOException {
        if (cachedSnmp == null) {
            TransportMapping<? extends TransportIpAddress> transportMapping = getTransport() == Transport.UDP ? new DefaultUdpTransportMapping() : new DefaultTcpTransportMapping();
            cachedSnmp = new Snmp(transportMapping);
            CounterSupport counterSupport = new CounterSupport();
            counterSupport.addCounterListener(new CounterListenerImpl());
            cachedSnmp.setCounterSupport(counterSupport);
            transportMapping.listen();
        }
        return cachedSnmp;
    }

    private int getSysUpTime() {
        return (int) ((System.currentTimeMillis() - startTime) / 10);
    }

    private String getOidName(OID oid) {
        return oid.toDottedString();
    }

    private class ResponseListenerImpl implements ResponseListener {

        @Override
        public <A extends Address> void onResponse(ResponseEvent<A> event) {
            PDU response = event.getResponse();
            if (response == null) {
                failed("Timed Out");
            } else if (response.getErrorStatus() != SNMP_ERROR_SUCCESS) {
                failed(response.getErrorStatusText());
            } else {
                success();
            }
        }

        private void failed(String status) {
            FAILED_BY_STATUS.count(status);
            FAILED_BY_TARGET.count(getSnmpAddress().toString());
        }

        private void success() {
            SUCCESS_BY_TARGET.count(getSnmpAddress().toString());
        }
    }

    class CounterListenerImpl implements CounterListener {

        @Override
        public void incrementCounter(CounterEvent event) {
            OIDS_METRIC.count(getOidName(event.getOid()));
        }
    }
}
