package net.microfalx.heimdall.rest.k6;

import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.heimdall.infrastructure.api.InfrastructureConstants;
import net.microfalx.heimdall.rest.api.Library;
import net.microfalx.heimdall.rest.api.Output;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.MemoryResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class K6SimulatorTest {

    @Mock
    private SimulationContext simulationContext;

    @BeforeEach
    void before() {
        Environment environment = Environment.create("test").attribute(InfrastructureConstants.API_KEY_VARIABLE, "key")
                .baseUri("https://test.k6.io").build();
        List<Library> libraries = List.of(Library.create(MemoryResource.create("lib1", "lib1.js")).type(Simulation.Type.K6).build(),
                Library.create(MemoryResource.create("lib2", "lib2.js")).type(Simulation.Type.K6).build());
        when(simulationContext.getLibraries()).thenReturn(libraries);
        when(simulationContext.getEnvironment()).thenReturn(environment);
    }

    @Test
    void simpleSimulation() throws IOException {
        K6Simulator simulator = new K6Simulator(createSimulation("simple_simulation.js"));
        Collection<Output> outputs = simulator.execute(simulationContext);
        assertEquals(1, outputs.size());
        Output output = outputs.iterator().next();
        assertEquals("Default", output.getName());
        assertEquals(2, output.getVus().getMaximum().orElse(0));
        assertEquals(2, output.getVusMax().getMaximum().orElse(0));
        assertEquals(1, output.getHttpRequests().getValue().getValue());
        assertTrue(output.getHttpRequestDuration().getAverage().orElse(0) > 0);
    }

    @Test
    void scenariosSimulation() throws IOException {
        K6Simulator simulator = new K6Simulator(createSimulation("scenarios_simulation.js"));
        Collection<Output> outputs = simulator.execute(simulationContext);
        assertEquals(2, outputs.size());
        Iterator<Output> iterator = outputs.iterator();
        Output output = iterator.next();
        assertEquals(1, output.getHttpRequests().getValue().getValue());
        output = iterator.next();
        assertEquals(1, output.getHttpRequests().getValue().getValue());

    }

    private Simulation createSimulation(String fileName) {
        return (Simulation) Simulation.create(ClassPathResource.file("scripts/" + fileName)).type(Simulation.Type.K6).build();
    }

}