package net.microfalx.heimdall.rest.core.overview;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.dataset.model.IdentityAware;
import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.lang.annotation.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Name("Scenarios")
@ReadOnly
public class Scenario extends IdentityAware<String> {

    @Position(10)
    @Description("The environment used during simulation")
    private Environment environment;

    @Position(10)
    @Description("The executed simulation (test plan)")
    private net.microfalx.heimdall.rest.api.Simulation simulation;

    @Position(10)
    @Description("The script executed during simulation")
    private net.microfalx.heimdall.rest.api.Scenario scenario;

    @Position(502)
    @Description("The average duration of the simulation")
    private Duration duration;

    @Position(500)
    @Description("The timestamp of the first simulation")
    @OrderBy(OrderBy.Direction.DESC)
    @CreatedDate
    @CreatedAt
    private LocalDateTime firstExecutionAt;

    @Position(501)
    @Description("The timestamp of the last simulation")
    @LastModifiedDate
    @ModifiedAt
    private LocalDateTime lastExecutionAt;

    @Position(502)
    @Description("The number of executions ")
    private int executionCount;

    @Position(502)
    @Description("The number of times the simulation has failed")
    private int failureCount;
}
