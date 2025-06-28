package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.heimdall.protocol.snmp.jpa.AgentSimulatorRuleRepository;
import net.microfalx.heimdall.protocol.snmp.mib.MibService;
import net.microfalx.resource.Resource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.rethrowException;
import static net.microfalx.lang.StringUtils.toIdentifier;

@Component
public class AgentSimulator implements InitializingBean {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AgentSimulator.class);
    private final Map<String, AgentSimulatorRule> rules = new HashMap<>();

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

    @Override
    public void afterPropertiesSet() throws Exception {
        load();
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
            net.microfalx.heimdall.protocol.snmp.jpa.AgentSimulatorRule agentSimulatorRule = ruleRepository.findByNaturalId(id).orElse(null);
            if (agentSimulatorRule == null) {
                agentSimulatorRule = new net.microfalx.heimdall.protocol.snmp.jpa.AgentSimulatorRule();
                agentSimulatorRule.setNaturalId(id);
                agentSimulatorRule.setName(resource.getName());
                agentSimulatorRule.setDescription(resource.getDescription());
            }
            agentSimulatorRule.setContent(resource.loadAsString());
            ruleRepository.save(agentSimulatorRule);
            load(resource);
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

    private void load() {
        ruleRepository.findAll().forEach(rule -> load(Resource.text(rule.getContent())));
        LOGGER.info("Load agent simulator rules from database");
    }

    private void load(Resource resource) {
        requireNonNull(resource);
        try {
            AgentSimulatorParser parser = new AgentSimulatorParser();
            Collection<AgentSimulatorRule> agentSimulatorRules = parser.parse(resource);
            for (AgentSimulatorRule rule : agentSimulatorRules) {
                rules.put(rule.getId(), rule);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load agent simulator rules from resource: {}", resource.getName(), e);
            rethrowException(e);
        }
    }
}
