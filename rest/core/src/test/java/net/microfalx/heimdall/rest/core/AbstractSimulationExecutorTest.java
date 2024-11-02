package net.microfalx.heimdall.rest.core;

import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.heimdall.infrastructure.api.InfrastructureConstants;
import net.microfalx.heimdall.rest.api.Library;
import net.microfalx.heimdall.rest.api.Output;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.resource.MemoryResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AbstractSimulationExecutorTest {

    private TestSimulatorExecutor simulatorExecutor;

    @Mock
    private Simulation simulation;

    @Mock
    private SimulationContext simulationContext;

    @BeforeEach
    void before() {
        Environment environment = Environment.create("test").attribute(InfrastructureConstants.API_KEY_VARIABLE, "key").build();
        simulation = Simulation.create(MemoryResource.create("data", "sim1.js")).type(Simulation.Type.K6).build();
        List<Library> libraries = List.of(Library.create(MemoryResource.create("lib1", "lib1.js")).type(Simulation.Type.K6).build(),
                Library.create(MemoryResource.create("lib2", "lib2.js")).type(Simulation.Type.K6).build());
        when(simulationContext.getLibraries()).thenReturn(libraries);
        when(simulationContext.getEnvironment()).thenReturn(environment);
        simulatorExecutor = new TestSimulatorExecutor(simulation);
    }

    @Test
    void simple() {
        Output output = simulatorExecutor.execute(simulationContext);
        assertNotNull(output);
        assertEquals(12, output.getDataReceived().getValue().asDouble());
    }

}