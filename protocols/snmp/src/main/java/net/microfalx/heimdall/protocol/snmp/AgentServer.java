package net.microfalx.heimdall.protocol.snmp;

import jakarta.annotation.PreDestroy;
import net.microfalx.heimdall.protocol.snmp.mib.MibService;
import net.microfalx.lang.EnumUtils;
import net.microfalx.lang.IOUtils;
import net.microfalx.lang.JvmUtils;
import net.microfalx.metrics.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.agent.*;
import org.snmp4j.agent.io.DefaultMOPersistenceProvider;
import org.snmp4j.agent.io.MOInputFactory;
import org.snmp4j.agent.io.MOPersistenceProvider;
import org.snmp4j.agent.mo.snmp.SNMPv2MIB;
import org.snmp4j.agent.mo.snmp.SnmpCommunityMIB;
import org.snmp4j.agent.mo.snmp.StorageType;
import org.snmp4j.agent.mo.snmp.VacmMIB;
import org.snmp4j.agent.security.MutableVACM;
import org.snmp4j.agent.security.VACM;
import org.snmp4j.cfg.EngineBootsCounterFile;
import org.snmp4j.cfg.EngineBootsProvider;
import org.snmp4j.event.CounterEvent;
import org.snmp4j.event.CounterListener;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.util.WorkerPool;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.getRootCauseMessage;
import static net.microfalx.lang.StringUtils.isNotEmpty;

@Component
public class AgentServer implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentServer.class);

    private static final Metrics AGENT_METRICS = SnmpUtils.METRICS.withGroup("Agent");

    @Autowired(required = false) private SnmpProperties properties = new SnmpProperties();
    @Autowired private SnmpService snmpService;
    @Autowired private MibService mibService;

    private final OctetString engineID = new OctetString(MPv3.createLocalEngineID());
    private AgentConfigManager agent;
    private MOServer server;
    private MessageDispatcher messageDispatcher;

    private File bootCounterFile;
    private File agentConfigFile;

    public void register(MOGroup group) {
        requireNonNull(group);
    }

    /**
     * Returns all registered managed objects in the SNMP agent.
     *
     * @return a non-null instance
     */
    public Iterator<Map.Entry<MOScope, ManagedObject<?>>> getManagedObjects() {
        return server.iterator();
    }

    /**
     * Registers a managed object with the SNMP agent.
     *
     * @param managedObject the managed object to register
     */
    public void register(ManagedObject<?> managedObject, boolean replace) {
        requireNonNull(managedObject);
        try {
            server.register(managedObject, CONTEXT_STRING);
        } catch (DuplicateRegistrationException e) {
            if (replace) {
                server.unregister(managedObject, CONTEXT_STRING);
                this.register(managedObject, false);
            } else {
                throw new SnmpException("Managed object: " + managedObject + " is already registered", e);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initConfigFiles();
        initMo();
        initAgent();
        initCommunity();
        initSecurity();
        initVACM();
        initListeners();
        initOther();
        start();
    }

    @PreDestroy
    public void destroy() {
        messageDispatcher.stop();
    }

    private void initConfigFiles() {
        File snmpDirectory = JvmUtils.getCacheDirectory("snmp");
        bootCounterFile = new File(snmpDirectory, "boot_counter.cfg");
        agentConfigFile = new File(snmpDirectory, "agent_config.cfg");
        try {
            IOUtils.initializeFile(agentConfigFile);
            IOUtils.initializeFile(bootCounterFile);
        } catch (IOException e) {
            LOGGER.warn("Failed to initialize SNMP configuration files, root cause: {}", getRootCauseMessage(e));
        }
    }

    private void initMo() {
        DefaultMOServer defaultServer = new DefaultMOServer();
        defaultServer.setDeadlockPreventionEnabled(true);
        defaultServer.addContext(CONTEXT_STRING);
        this.server = new AgentMoServer(defaultServer);
    }

    private void initSecurity() {
        USM usm = agent.getUsm();
        UsmUser user = new UsmUser(new OctetString(properties.getAgentUserName()), properties.getAuthenticationProtocolOid(), new OctetString(properties.getAuthenticationPassword()), properties.getPrivacyProtocolOid(), new OctetString(properties.getPrivacyPassword()));
        usm.addUser(user.getSecurityName(), user);
    }

    private void initAgent() {
        messageDispatcher = snmpService.createDispatcher(SnmpMode.AGENT);
        MOServer[] moServers = {server};
        agent = new Agent(engineID, messageDispatcher, null, moServers, snmpService.getWorkerPool(), null, new DefaultMOPersistenceProvider(moServers, agentConfigFile.getAbsolutePath()), new EngineBootsCounterFile(bootCounterFile));
        agent.initialize();
    }

    private void initListeners() {
        CounterListenerImpl counterListener = new CounterListenerImpl();
        agent.getCommandProcessor().addCounterListener(counterListener);
        agent.getCounterSupport().addCounterListener(counterListener);
    }

    private void initOther() {
        // internally, the agent accesses a few things under a null context, so we need to register things
        DefaultMOServer dummyServer = new DefaultMOServer();
        SNMPv2MIB snmPv2MIB = agent.getSNMPv2MIB();
        try {
            snmPv2MIB.registerMOs(dummyServer, null);
        } catch (DuplicateRegistrationException e) {
            // no duplicates here
        }
        snmpService.getSimulator().setAgentServer(this);
    }

    private void initCommunity() {
        SnmpCommunityMIB snmpCommunityMIB = agent.getSnmpCommunityMIB();
        snmpCommunityMIB.addSnmpCommunityEntry(SECURITY_NAME, new OctetString(properties.getAgentComunityString()), SECURITY_NAME, engineID, CONTEXT_STRING, SECURITY_NAME, StorageType.nonVolatile);
    }

    private void initVACM() {
        VacmMIB vacm = agent.getVacmMIB();
        vacm.addGroup(SecurityModel.SECURITY_MODEL_SNMPv2c, SECURITY_NAME, SECURITY_NAME, StorageType.nonVolatile);
        vacm.addAccess(SECURITY_NAME, CONTEXT_STRING, SecurityModel.SECURITY_MODEL_ANY, SecurityLevel.NOAUTH_NOPRIV, MutableVACM.VACM_MATCH_EXACT, FULL_READ_VIEW, FULL_WRITE_VIEW, FULL_NOTIFY_VIEW, StorageType.nonVolatile);
        vacm.addViewTreeFamily(FULL_READ_VIEW, new OID("1.3"), new OctetString(), VacmMIB.vacmViewIncluded, StorageType.nonVolatile);
    }

    private void start() {
        // initialize & run agent
        agent.setupProxyForwarder();
        agent.run();
    }

    private String getOidName(OID oid) {
        String name = mibService.findName(oid.toDottedString(), false, true);
        return isNotEmpty(name) ? name : oid.toDottedString();
    }

    static class Agent extends AgentConfigManager {

        public Agent(OctetString agentsOwnEngineID, MessageDispatcher messageDispatcher, VACM vacm, MOServer[] moServers, WorkerPool workerPool, MOInputFactory configurationFactory, MOPersistenceProvider persistenceProvider, EngineBootsProvider engineBootsProvider) {
            super(agentsOwnEngineID, messageDispatcher, vacm, moServers, workerPool, configurationFactory, persistenceProvider, engineBootsProvider);
            defaultContext = CONTEXT_STRING;
        }
    }

    class CounterListenerImpl implements CounterListener {

        private static final Metrics COUNTER_METRICS = AGENT_METRICS.withGroup(EnumUtils.toLabel(SnmpMode.AGENT)).withGroup("Counter");

        @Override
        public void incrementCounter(CounterEvent event) {
            String oidName = getOidName(event.getOid());
            COUNTER_METRICS.count(oidName);
        }
    }

    static {
        SnmpLogger.init();
    }

    private static final OctetString CONTEXT_STRING = new OctetString("heimdall");
    private static final OctetString SECURITY_NAME = new OctetString("heimdall");
    private static final OctetString FULL_READ_VIEW = new OctetString("fullReadView");
    private static final OctetString FULL_WRITE_VIEW = new OctetString("fullWriteView");
    private static final OctetString FULL_NOTIFY_VIEW = new OctetString("fullNotifyView");

}
