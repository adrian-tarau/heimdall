package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.lang.UriUtils;
import net.microfalx.resource.Resource;
import net.microfalx.resource.UrlResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.agent.request.SubRequest;

import java.io.IOException;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class AgentSimulatorParserTest {

    private AgentSimulatorParser parser;

    @BeforeEach
    void setUp() {
        parser = new AgentSimulatorParser();
    }

    @Test
    void parseFileWithNoFunctions() throws IOException {
        Resource resource = Resource.url(UriUtils.parseUrl("https://raw.githubusercontent.com/etingof/snmpsim/refs/heads/master/data/1.3.6.1.6.1.1.0/127.0.0.1.snmprec"));
        Collection<AgentSimulatorRule> agentSimulatorRules = parser.parse(resource);
        assertNotNull(agentSimulatorRules);
        AgentSimulatorRule agentSimulatorRule = agentSimulatorRules.stream().findFirst().get();
        assertNotNull(agentSimulatorRule.getManagedObject());
        assertNotNull(agentSimulatorRule.getValue());
        assertEquals(resource.loadAsString().split("\n").length, agentSimulatorRules.size());
    }

    @Test
    void parseFileWithNumericFunctions() throws IOException {
        Resource resource = Resource.url(UriUtils.parseUrl("https://raw.githubusercontent.com/etingof/snmpsim/refs/heads/master/data/variation/virtualtable.snmprec"));
        Collection<AgentSimulatorRule> agentSimulatorRules = parser.parse(resource);
        assertNotNull(agentSimulatorRules);
        assertEquals(resource.loadAsString().split("\n").length, agentSimulatorRules.size());
        AgentSimulatorRule agentSimulatorRule = agentSimulatorRules.stream().filter(a ->
                a.getFunction() instanceof AgentSimulatorRule.LinearFunction).findFirst().get();
        assertNotNull(agentSimulatorRule.getManagedObject());
        assertNotNull(agentSimulatorRule.getValue());
    }
}