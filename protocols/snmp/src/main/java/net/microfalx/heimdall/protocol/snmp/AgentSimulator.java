package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.bootstrap.core.async.ThreadPoolFactory;
import net.microfalx.heimdall.protocol.snmp.jpa.AgentSimulatorRuleRepository;
import net.microfalx.heimdall.protocol.snmp.mib.MibService;
import net.microfalx.resource.Resource;
import net.microfalx.threadpool.ThreadPool;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.rethrowException;
import static net.microfalx.lang.StringUtils.toIdentifier;

@Component
public class AgentSimulator implements InitializingBean {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AgentSimulator.class);

    @Autowired(required = false)
    private SnmpProperties properties = new SnmpProperties();

    @Autowired
    private AgentServer agentServer;

    @Autowired
    private SnmpService snmpService;

    @Autowired
    private MibService mibService;

    @Autowired
    private AgentSimulatorRuleRepository ruleRepository;

    private ThreadPool threadPool;

    private volatile RuleHolder rules = new RuleHolder();

    @Override
    public void afterPropertiesSet() throws Exception {
        initThreadPools();
        load();
        scheduleDynamicRules();
    }

    /**
     * Persists the snmp agent simulation file into the database.
     *
     * @param resource a non-null instance
     */
    public void persist(Resource resource) {
        requireNonNull(resource);
        try {
            String id = toIdentifier(resource.getName());
            net.microfalx.heimdall.protocol.snmp.jpa.AgentSimulatorRule rule = ruleRepository.findByNaturalId(id).orElse(null);
            if (rule == null) {
                rule = new net.microfalx.heimdall.protocol.snmp.jpa.AgentSimulatorRule();
                rule.setNaturalId(id);
                rule.setName(resource.getName());
                rule.setDescription(resource.getDescription());
            }
            rule.setContent(resource.loadAsString());
            ruleRepository.save(rule);
            load(resource, rule.isEnabled(), this.rules);
            registerStaticRules();
        } catch (IOException e) {
            rethrowException(e);
        }
    }

    /**
     * Finds an agent simulator rule by its identifier.
     *
     * @param id the identifier of the rule, which is the natural ID of the resource
     * @return a non-null instance of {@link AgentSimulatorRule} if found, or null if not found
     */
    public AgentSimulatorRule findById(String id) {
        requireNonNull(id);
        return rules.get(id);
    }

    private void initThreadPools() {
        threadPool = ThreadPoolFactory.create("Agent").create();
    }

    private void registerStaticRules() {
        ThreadPool.get().execute(new RuleTask(rules.staticRules.values()));
    }

    private void scheduleDynamicRules() {
        ThreadPool.get().scheduleAtFixedRate(new RuleTask(rules.dynamicRules.values()), properties.getSimulatorInterval());
    }

    private void load() {
        LOGGER.info("Load agent simulator rules from database");
        RuleHolder rules = new RuleHolder();
        ruleRepository.findAll().forEach(rule -> load(Resource.text(rule.getContent()), rule.isEnabled(), rules));
        this.rules = rules;
        registerStaticRules();
    }

    private void load(Resource resource, boolean enabled, RuleHolder rules) {
        requireNonNull(resource);
        requireNonNull(rules);
        try {
            AgentSimulatorParser parser = new AgentSimulatorParser();
            Collection<AgentSimulatorRule> agentSimulatorRules = parser.parse(resource);
            for (AgentSimulatorRule rule : agentSimulatorRules) {
                rule.enabled = enabled;
                if (rule.isDynamic()) {
                    rules.dynamicRules.put(rule.getId(), rule);
                } else {
                    rules.staticRules.put(rule.getId(), rule);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load agent simulator rules from resource: {}", resource.getName(), e);
            rethrowException(e);
        }
    }

    private static class RuleHolder {

        private final Map<String, AgentSimulatorRule> staticRules = new ConcurrentHashMap<>();
        private final Map<String, AgentSimulatorRule> dynamicRules = new ConcurrentHashMap<>();

        private AgentSimulatorRule get(String id) {
            requireNonNull(id);
            AgentSimulatorRule rule = staticRules.get(id);
            if (rule == null) rule = dynamicRules.get(id);
            return rule;
        }
    }

    class RuleTask implements Runnable {

        private final Collection<AgentSimulatorRule> agentSimulatorRules;

        public RuleTask(Collection<AgentSimulatorRule> agentSimulatorRules) {
            this.agentSimulatorRules = agentSimulatorRules;
        }

        @Override
        public void run() {
            try {
                agentSimulatorRules.forEach(rule -> agentServer.register(rule.getManagedObject(), true));
            } catch (Exception e) {
                LOGGER.atError().setCause(e).log("Failed to register agent simulator rules");
            }
        }
    }
}
