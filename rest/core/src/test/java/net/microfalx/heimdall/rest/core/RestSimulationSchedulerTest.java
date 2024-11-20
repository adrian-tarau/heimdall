package net.microfalx.heimdall.rest.core;

import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.heimdall.rest.api.Schedule;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.lang.UriUtils;
import net.microfalx.resource.MemoryResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RestSimulationSchedulerTest {

    @Mock
    private RestServiceImpl restService;

    @Mock
    private TaskScheduler scheduler;

    @Mock
    private TaskExecutor executor;

    @InjectMocks
    private RestSimulationScheduler restSimulationScheduler;

    @BeforeEach
    void setUp() {
        Simulation simulation = (Simulation) Simulation.create(MemoryResource.create("This is a simulation"))
                .type(Simulation.Type.K6).build();
        Environment.Builder builder = Environment.create();
        builder.baseUri(UriUtils.parseUri("http://localhost:8080").toASCIIString());
        builder.id("123456");
        Schedule.Builder schedule = new Schedule.Builder().simulation(simulation)
                .environment(builder.build()).interval(Duration.of(5, ChronoUnit.SECONDS));
        schedule.description("This is a schedule description");
        schedule.name("This is a schedule name");
        schedule.tag("This isa schedule tag");
        schedule.id("09876");
        when(restService.getProperties()).thenReturn(new RestProperties());
        when(restService.getSchedules()).thenReturn(Collections.singletonList(schedule.build()));
        when(restService.simulate(schedule.build().getSimulation(), schedule.build().getEnvironment())).thenReturn(Collections.emptyList());
        restSimulationScheduler.initialize(restService);
    }

    @Test
    void reload() {
        restSimulationScheduler.reload();
        verify(scheduler).schedule(any(Runnable.class), any(Trigger.class));
    }
}