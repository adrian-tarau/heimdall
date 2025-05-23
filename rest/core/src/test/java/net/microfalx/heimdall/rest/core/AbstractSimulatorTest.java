package net.microfalx.heimdall.rest.core;

import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.heimdall.infrastructure.api.InfrastructureConstants;
import net.microfalx.heimdall.rest.api.*;
import net.microfalx.resource.MemoryResource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AbstractSimulatorTest {

    private TestSimulator simulator;

    @Mock
    private Simulation simulation;

    @Mock
    private SimulationContext simulationContext;

    @BeforeEach
    void before() {
        Environment environment = Environment.create("test").attribute(InfrastructureConstants.API_KEY_VARIABLE, "key").build();
        simulation = (Simulation) Simulation.create(MemoryResource.create("data", "sim1.js")).type(Simulation.Type.K6).build();
        List<Library> libraries = List.of(Library.create(MemoryResource.create("lib1", "lib1.js")).type(Simulation.Type.K6).build(),
                Library.create(MemoryResource.create("lib2", "lib2.js")).type(Simulation.Type.K6).build());
        when(simulationContext.getLibraries()).thenReturn(libraries);
        when(simulationContext.getEnvironment()).thenReturn(environment);

        simulator = new TestSimulator(simulation);
    }

    @Test
    void update() {
        assertNotNull(simulator.unpack());
    }

    @Test
    void install() throws IOException {
        simulator.getWorkspace().delete();
        assertNotNull(simulator.unpack());
    }

    @Test
    void execute() throws IOException {
        Result result = simulator.execute(simulationContext);
        assertNotNull(result);
        assertNotNull(result.getSimulation());
        assertNotNull(result.getReport());
        assertNotNull(result.getLogs());
        Collection<Output> outputs = result.getOutputs();
        assertEquals(1, outputs.size());
        assertThat(outputs.iterator().next().getDataReceived().getValue().asDouble()).isEqualTo(12);
        assertTrue(simulator.getLogs().exists());
        Assertions.assertThat(simulator.getLogs().loadAsString()).contains("aaa");
    }

}