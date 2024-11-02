package net.microfalx.heimdall.rest.jmeter;

import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.heimdall.infrastructure.api.InfrastructureConstants;
import net.microfalx.heimdall.rest.api.Library;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JmeterSimulatorTest {

    @Mock
    private SimulationContext simulationContext;

    private JmeterSimulator simulator;

    @BeforeEach
    void before() {
        simulator = new JmeterSimulator();
        Environment environment = Environment.create("test").attribute(InfrastructureConstants.API_KEY_VARIABLE, "key").build();
        List<Library> libraries = List.of(Library.create(MemoryResource.create("lib1", "lib1.js")).type(Simulation.Type.K6).build(),
                Library.create(MemoryResource.create("lib2", "lib2.js")).type(Simulation.Type.K6).build());
        when(simulationContext.getLibraries()).thenReturn(libraries);
        when(simulationContext.getEnvironment()).thenReturn(environment);
    }

    @Test
    void simpleSimulation() throws IOException {
        Resource resource = simulator.execute(createSimulation("simple_simulation.jmx"), simulationContext);
        assertOutput(resource);
    }

    private Simulation createSimulation(String fileName) {
        return Simulation.create(ClassPathResource.file("scripts/" + fileName)).type(Simulation.Type.JMETER).build();
    }

    private void assertOutput(Resource resource) throws IOException {
        assertNotNull(resource);
        org.assertj.core.api.Assertions.assertThat(resource.loadAsString())
                .contains("test.k6.io").contains("home_page").contains("Thread Group");
    }

}