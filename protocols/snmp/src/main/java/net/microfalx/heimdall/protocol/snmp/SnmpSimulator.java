package net.microfalx.heimdall.protocol.snmp;

import com.google.common.collect.Iterators;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.ProtocolSimulator;
import net.microfalx.heimdall.protocol.core.ProtocolSimulatorProperties;
import net.microfalx.heimdall.protocol.snmp.mib.*;
import net.microfalx.resource.ClassPathResource;
import org.jsmiparser.smi.SmiPrimitiveType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static net.microfalx.lang.ConcurrencyUtils.await;

@Component
public class SnmpSimulator extends ProtocolSimulator<SnmpEvent, SnmpClient> {

    private final SnmpProperties configuration;
    private final MibService mibService;
    private final CountDownLatch latch = new CountDownLatch(1);
    private volatile List<MibModule> modules = Collections.emptyList();

    public SnmpSimulator(ProtocolSimulatorProperties properties, SnmpProperties configuration, MibService mibService) {
        super(properties);
        this.configuration = configuration;
        this.mibService = mibService;
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
    protected void initializeData() {
        if (getProperties().isUseSamples()) {
            mibService.getThreadPool().execute(new LoadMibsWorker());
        } else {
            latch.countDown();
        }
    }

    @Override
    protected Collection<SnmpClient> createClients() {
        SnmpClient udpClient = new SnmpClient();
        udpClient.setPort(configuration.getUdpPort());
        return Arrays.asList(udpClient);
    }

    @Override
    protected void simulate(SnmpClient client, Address sourceAddress, Address targetAddress, int index) throws IOException {
        await(latch);
        MibModule module = getNextModule();
        if (module == null) return;
        SnmpEvent trap = new SnmpEvent();
        trap.setSource(sourceAddress);
        trap.addTarget(targetAddress);
        trap.setBody(Body.create(getRandomText()));
        updateClient(client, trap, module);
        updateBindings(client, trap, module);
        client.send(trap);
    }

    private void updateClient(SnmpClient client, SnmpEvent trap, MibModule module) {
        client.setMessageOid(SnmpClient.DEFAULT_MESSAGE_OID);
        Set<String> messageOids = module.getMessageOids();
        if (!messageOids.isEmpty()) {
            client.setMessageOid(Iterators.get(messageOids.iterator(), random.nextInt(messageOids.size())));
        }
    }

    private void updateBindings(SnmpClient client, SnmpEvent trap, MibModule module) {
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
            MibVariable severityVariable = mibService.findVariable(severity);
            if (severityVariable != null) trap.add(severity, getNextValue(severityVariable));
        }
        Set<String> sentAtOids = module.getSentAtOids();
        if (!sentAtOids.isEmpty()) {
            String sentAtOid = Iterators.get(sentAtOids.iterator(), random.nextInt(sentAtOids.size()));
            MibVariable severityVariable = mibService.findVariable(sentAtOid);
            if (severityVariable != null) trap.add(sentAtOid, getNextValue(severityVariable));
        }
    }

    private String getNextValue(MibVariable severityVariable) {
        SmiPrimitiveType type = severityVariable.getType();
        if (severityVariable.isEnum()) {
            Collection<MibNamedNumber> enumValues = severityVariable.getEnumValues();
            MibNamedNumber mibNamedNumber = Iterators.get(enumValues.iterator(), random.nextInt(enumValues.size()));
            return mibNamedNumber.getName();
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
        if (modules.isEmpty()) {
            modules = mibService.getModules().stream().filter(module -> module.getType() == MibType.USER).collect(Collectors.toList());
        }
        return modules.isEmpty() ? null : modules.get(random.nextInt(modules.size()));
    }

    private class LoadMibsWorker implements Runnable {

        @Override
        public void run() {
            try {
                mibService.loadModules(ClassPathResource.directory("simulator/snmp/mib"));
            } finally {
                latch.countDown();
            }
        }
    }
}
