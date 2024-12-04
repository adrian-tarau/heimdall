package net.microfalx.heimdall.rest.core;

import net.microfalx.bootstrap.metrics.Value;
import net.microfalx.heimdall.rest.api.Scenario;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.resource.MemoryResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RestUtilsTest {

    private Scenario scenario;

    @BeforeEach
    void setUp() {
        Simulation simulation = (Simulation) Simulation.create(MemoryResource.create("This is a simulation"))
                .type(Simulation.Type.K6).build();
        Scenario.Builder builder = new Scenario.Builder("1234");
        builder.toleratingThreshold(Duration.ofMillis(3)).frustratingThreshold(Duration.ofMillis(12))
                .simulation(simulation).function("").startTime(Duration.ZERO)
                .gracefulStop(Duration.ZERO).step(null).name("Scenario").description("");
        scenario = builder.build();
    }

    @Test
    void getApdexScore() {
        List<Value> values = List.of(Value.create(1), Value.create(2), Value.create(3), Value.create(4), Value.create(5));
        assertEquals(0.699999988079071, RestUtils.getApdexScore(scenario, values));
    }
}