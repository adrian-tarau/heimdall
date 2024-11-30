package net.microfalx.heimdall.rest.core.overview;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.dataset.model.NamedIdentityAware;
import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.lang.annotation.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Name("Running")
@ReadOnly
public class SimulationRunning extends NamedIdentityAware<String> {

    @Position(4)
    @Description("The environment used during simulation")
    private Environment environment;

    @Position(100)
    @Formattable(maximumLength = 40)
    @Description("The script executed during simulation")
    private String script;

    @Position(500)
    @Description("The timestamp when the simulation was started")
    @OrderBy(OrderBy.Direction.DESC)
    @CreatedDate
    @CreatedAt
    private LocalDateTime startedAt;

    @Position(501)
    @Description("The duration of the simulation")
    private Duration duration;

    @Position(501)
    @Description("The user that triggered the simulation (if not present, it was triggered by a scheduler)")
    private String user;
}
