package net.microfalx.heimdall.protocol.snmp;

import com.google.common.collect.Iterators;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.Event;
import net.microfalx.heimdall.protocol.core.simulator.ProtocolSimulator;
import net.microfalx.heimdall.protocol.core.simulator.ProtocolSimulatorProperties;
import net.microfalx.heimdall.protocol.snmp.mib.*;
import net.microfalx.lang.TimeUtils;
import net.microfalx.resource.ClassPathResource;
import org.jsmiparser.smi.SmiPrimitiveType;
import org.snmp4j.PDU;
import org.snmp4j.agent.MOScope;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.agent.RegisteredManagedObject;
import org.snmp4j.smi.OID;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ConcurrencyUtils.await;
import static net.microfalx.lang.TimeUtils.FIVE_MINUTE;
import static net.microfalx.lang.TimeUtils.millisSince;

@Component
public class SnmpSimulator extends ProtocolSimulator<SnmpEvent, SnmpClient> {

    private final SnmpProperties properties;
    private final CountDownLatch latch = new CountDownLatch(1);

    private volatile List<MibModule> modules = Collections.emptyList();
    private volatile long lastModuleUpdates = TimeUtils.oneHourAgo();
    private volatile List<OID> oids = Collections.emptyList();
    private volatile List<OID> oidBases = Collections.emptyList();
    private volatile long lastOidUpdates = TimeUtils.oneHourAgo();

    private SnmpService snmpService;
    private AgentServer agentServer;

    public SnmpSimulator(ProtocolSimulatorProperties properties, SnmpProperties snmpProperties) {
        super(properties);
        this.properties = snmpProperties;
    }

    @Override
    protected Event.Type getEventType() {
        return Event.Type.SNMP;
    }

    @Override
    public boolean isEnabled() {
        if (snmpService == null || agentServer == null) return false;
        if (super.isEnabled()) {
            return true;
        } else {
            return properties.isSimulatorEnabled();
        }
    }

    public MibService getMibService() {
        return snmpService.getMibService();
    }

    public void setSnmpService(SnmpService snmpService) {
        requireNonNull(snmpService);
        this.snmpService = snmpService;
        if (getProperties().isUseSamples()) {
            snmpService.getThreadPool().execute(new LoadMibsWorker());
        } else {
            latch.countDown();
        }
    }

    public void setAgentServer(AgentServer agentServer) {
        requireNonNull(agentServer);
        this.agentServer = agentServer;
    }

    @Override
    protected Address createSourceAddress() {
        return Address.create(Address.Type.HOSTNAME, getRandomDomainOrIp(true));
    }

    @Override
    protected Address createTargetAddress() {
        return Address.create(Address.Type.HOSTNAME, getRandomDomainOrIp(false));
    }

    @Override
    protected Collection<SnmpClient> createClients() {
        SnmpClient agentUdpClient = new SnmpClient(SnmpMode.AGENT);
        agentUdpClient.setPort(properties.getAgentUdpPort());
        SnmpClient agentTcpClient = new SnmpClient(SnmpMode.AGENT);
        agentTcpClient.setPort(properties.getAgentTcpPort());

        SnmpClient trapUdpClient = new SnmpClient(SnmpMode.TRAP);
        trapUdpClient.setPort(properties.getTrapUdpPort());
        SnmpClient trapTcpClient = new SnmpClient(SnmpMode.TRAP);
        trapTcpClient.setPort(properties.getTrapTcpPort());
        return Arrays.asList(agentTcpClient, agentTcpClient, trapTcpClient, trapUdpClient);
    }

    @Override
    protected void simulate(SnmpClient client, Address sourceAddress, Address targetAddress, int index) throws IOException {
        await(latch);
        if (client.getMode() == SnmpMode.AGENT) {
            simulateGet(client, sourceAddress, targetAddress, index);
        } else {
            simulateTrap(client, sourceAddress, targetAddress, index);
        }
    }

    private void simulateGet(SnmpClient client, Address sourceAddress, Address targetAddress, int index) throws IOException {
        SnmpEvent request = new SnmpEvent();
        request.setSource(sourceAddress);
        request.addTarget(targetAddress);
        request.setPduType(PDU.GET);
        if (random.nextFloat() >= 0.8) {
            request.setPduType(PDU.GETBULK);
            request.setMaxRepetitions(10 + random.nextInt(10));
        }
        request.setBody(Body.create("dummy"));
        updateAgentBindings(client, request);
        if (!request.isEmpty()) client.send(request);
    }

    private void simulateTrap(SnmpClient client, Address sourceAddress, Address targetAddress, int index) throws IOException {
        MibModule module = getNextModule();
        if (module == null) return;
        SnmpEvent trap = new SnmpEvent();
        trap.setSource(sourceAddress);
        trap.addTarget(targetAddress);
        trap.setBody(Body.create(getRandomText()));
        updateTrapClient(client, trap, module);
        updateTrapBindings(client, trap, module);
        client.send(trap);
    }

    private void updateAgentBindings(SnmpClient client, SnmpEvent request) {
        OID oid;
        if (request.getPduType() == PDU.GET) {
            oid = getNextOID();
        } else {
            oid = getNextOIDBase();
        }
        if (oid != null) request.add(oid.toDottedString(), 1);
    }

    private void updateTrapClient(SnmpClient client, SnmpEvent trap, MibModule module) {
        client.setMessageOid(SnmpClient.DEFAULT_MESSAGE_OID);
        Set<String> messageOids = module.getMessageOids();
        if (!messageOids.isEmpty()) {
            client.setMessageOid(Iterators.get(messageOids.iterator(), random.nextInt(messageOids.size())));
        }
    }

    private void updateTrapBindings(SnmpClient client, SnmpEvent trap, MibModule module) {
        Collection<MibVariable> variables = module.getVariables();
        int count = 2 + random.nextInt(8);
        int start = variables.size() > 30 ? random.nextInt(variables.size() / 2) : 0;
        int step = variables.size() > 30 ? random.nextInt(5) : 1;
        Iterator<MibVariable> variableIterator = variables.iterator();
        Iterators.advance(variableIterator, start);
        for (int i = start; i < count && variableIterator.hasNext(); i++) {
            MibVariable variable = variableIterator.next();
            trap.add(variable.getOid(), getNextValue(variable));
            if (step > 1) Iterators.advance(variableIterator, random.nextInt(step));
        }
        Set<String> severityOids = module.getSeverityOids();
        if (!severityOids.isEmpty()) {
            String severity = Iterators.get(severityOids.iterator(), random.nextInt(severityOids.size()));
            MibVariable severityVariable = getMibService().findVariable(severity);
            if (severityVariable != null) trap.add(severity, getNextValue(severityVariable));
        }
        Set<String> sentAtOids = module.getSentAtOids();
        if (!sentAtOids.isEmpty()) {
            String sentAtOid = Iterators.get(sentAtOids.iterator(), random.nextInt(sentAtOids.size()));
            MibVariable severityVariable = getMibService().findVariable(sentAtOid);
            if (severityVariable != null) trap.add(sentAtOid, getNextValue(severityVariable));
        }
    }

    private String getNextValue(MibVariable severityVariable) {
        SmiPrimitiveType type = severityVariable.getType();
        if (severityVariable.isEnum()) {
            Collection<MibNamedNumber> enumValues = severityVariable.getEnumValues();
            MibNamedNumber mibNamedNumber = Iterators.get(enumValues.iterator(), random.nextInt(enumValues.size()));
            return mibNamedNumber != null ? mibNamedNumber.getName() : null;
        } else if (severityVariable.isNumber()) {
            return Integer.toString(random.nextInt(1000));
        } else if (type == SmiPrimitiveType.IP_ADDRESS) {
            return getRandomDomainOrIp(true);
        } else if (type == SmiPrimitiveType.OCTET_STRING) {
            return getRandomName();
        } else {
            return null;
        }
    }

    private MibModule getNextModule() {
        if (modules.isEmpty() || millisSince(lastModuleUpdates) >= FIVE_MINUTE) {
            modules = getMibService().getModules().stream().filter(module -> module.getType() == MibType.USER).collect(Collectors.toList());
            lastModuleUpdates = currentTimeMillis();
        }
        return modules.isEmpty() ? null : modules.get(random.nextInt(modules.size()));
    }

    private OID getNextOID() {
        updateOids();
        return oids.isEmpty() ? null : oids.get(random.nextInt(oids.size()));
    }

    private OID getNextOIDBase() {
        updateOids();
        return oids.isEmpty() ? null : oidBases.get(random.nextInt(oidBases.size()));
    }

    private void updateOids() {
        if (!(oids.isEmpty() || millisSince(lastOidUpdates) >= FIVE_MINUTE)) return;
        List<OID> oids = new ArrayList<>();
        List<OID> oidBases = new ArrayList<>();
        Iterator<Map.Entry<MOScope, ManagedObject<?>>> managedObjects = agentServer.getManagedObjects();
        while (managedObjects.hasNext()) {
            ManagedObject<?> managedObject = managedObjects.next().getValue();
            OID oid = null;
            if (managedObject instanceof RegisteredManagedObject<?> registeredManagedObject) {
                oid = registeredManagedObject.getID();
            }
            if (oids != null) {
                oids.add(oid);
                oidBases.add(SnmpUtils.getBaseOid(oid));
            }
        }
        this.lastOidUpdates = currentTimeMillis();
        this.oids = oids;
        this.oidBases = oidBases;
    }

    private class LoadMibsWorker implements Runnable {

        @Override
        public void run() {
            try {
                getMibService().loadModules(ClassPathResource.directory("simulator/snmp/mib"));
            } finally {
                latch.countDown();
            }
        }
    }
}
