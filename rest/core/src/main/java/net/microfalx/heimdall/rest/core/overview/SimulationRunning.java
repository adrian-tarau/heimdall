package net.microfalx.heimdall.rest.core.overview;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.dataset.model.NamedIdentityAware;
import net.microfalx.lang.annotation.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Name("Simulations")
@ReadOnly
public class SimulationRunning extends NamedIdentityAware<String> {

    @Position(100)
    @Visible(modes = {Visible.Mode.BROWSE})
    @Description("The script executed during simulation")
    private String script;

    @Position(500)
    @Visible(modes = {Visible.Mode.BROWSE})
    @Description("The timestamp when the simulation was started")
    @OrderBy(OrderBy.Direction.DESC)
    @CreatedDate
    @CreatedAt
    private LocalDateTime startedAt;

    @Position(501)
    @Visible(modes = {Visible.Mode.BROWSE})
    @Description("The duration of the simulation")
    private Duration duration;
}
