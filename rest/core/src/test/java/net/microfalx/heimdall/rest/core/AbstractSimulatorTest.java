package net.microfalx.heimdall.rest.core;

import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.heimdall.infrastructure.api.InfrastructureConstants;
import net.microfalx.heimdall.rest.api.Library;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.resource.MemoryResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbstractSimulatorTest {

    private TestSimulator simulator;

    @Mock
    private Simulation simulation;

    @Mock
    private SimulationContext simulationContext;

    @BeforeEach
    void before() {
        simulator = new TestSimulator();
        Environment environment = Environment.create("test").attribute(InfrastructureConstants.API_KEY_VARIABLE, "key").build();
        simulation = Simulation.create(MemoryResource.create("data", "sim1.js")).type(Simulation.Type.K6).build();
        List<Library> libraries = List.of(Library.create(MemoryResource.create("lib1", "lib1.js")).type(Simulation.Type.K6).build(),
                Library.create(MemoryResource.create("lib2", "lib2.js")).type(Simulation.Type.K6).build());
        when(simulationContext.getLibraries()).thenReturn(libraries);
        when(simulationContext.getEnvironment()).thenReturn(environment);
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
    void execute() {
        assertNotNull(simulator.execute(simulation, simulationContext));
    }

    public static class TestSimulator extends AbstractSimulator {

        @Override
        protected Options resolveOptions() {
            return new Options("sim").setName("Test Simulator").setVersion("1.0")
                    .setPackage("sim-${VERSION}.zip").setWindowsExecutable("sim.bat").setLinuxExecutable("sim.sh")
                    .addFiles("bin/sim.bat", "config/settings.properties", "lib/slf4j-api-2.0.16.jar");
        }

        @Override
        protected void update(List<String> arguments, File input, File output, SimulationContext context) {
            arguments.addAll(Arrays.asList("-i", input.getName(), "-o", output.getName()));
        }
    }

}