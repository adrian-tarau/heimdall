package net.microfalx.heimdall.rest.core;

import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.heimdall.infrastructure.api.InfrastructureConstants;
import net.microfalx.heimdall.rest.api.Output;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.lang.Nameable;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AbstractOutputParserTest {

    @Mock
    private AbstractSimulator simulator;

    @Mock
    private Simulation simulation;

    @Mock
    private SimulationContext simulatorContext;

    @BeforeEach
    void setup() {
        Environment environment = Environment.create("test").attribute(InfrastructureConstants.API_KEY_VARIABLE, "key").build();
        when(simulatorContext.getEnvironment()).thenReturn(environment);
        when(simulator.getStartTime()).thenReturn(LocalDateTime.now());
        when(simulator.getEndTime()).thenReturn(LocalDateTime.now().plusMinutes(1));
    }

    @Test
    void noResult() throws IOException {
        TestOutputParser parser = createParser(MemoryResource.create(""));
        Collection<Output> outputs = parser.parse();
        assertEquals(0, outputs.size());
    }

    @Test
    void k6SingleScenario() throws IOException {
        TestOutputParser parser = createParser(ClassPathResource.file("results/k6_single_scenario.csv"));
        Collection<Output> outputs = parser.parse();
        assertEquals(1, outputs.size());
        Output output = outputs.stream().iterator().next();
        assertEquals("Default", output.getName());
        assertNotNull(output.getStartTime());
        assertNotNull(output.getEndTime());
        assertTrue(output.getDuration().toMillis() > 0);
        assertTrue(output.getHttpRequests().getValue().asDouble() > 0);
        assertTrue(output.getIterations().getValue().asDouble() > 0);
        assertTrue(output.getIterationDuration().getAverage().orElse(0) > 0);
    }

    @Test
    void k6MultipleScenarios() throws IOException {
        TestOutputParser parser = createParser(ClassPathResource.file("results/k6_multiple_scenarios.csv"));
        Collection<Output> outputs = parser.parse().stream().sorted(Comparator.comparing(Nameable::getName)).toList();
        assertEquals(2, outputs.size());
        Iterator<Output> iterator = outputs.stream().iterator();
        Output output = iterator.next();
        assertEquals("Per Vu Scenario", output.getName());
        assertTrue(output.getHttpRequests().getValue().asDouble() > 0);
        output = iterator.next();
        assertEquals("Shared Iter Scenario", output.getName());
        assertTrue(output.getHttpRequests().getValue().asDouble() > 0);
    }

    private TestOutputParser createParser(Resource resource) {
        return new TestOutputParser(simulator, simulation, simulatorContext, resource);
    }

}