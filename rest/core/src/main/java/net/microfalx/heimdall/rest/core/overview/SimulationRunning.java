package net.microfalx.heimdall.rest.core.overview;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.*;
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
    @Filterable
    private String script;

    @Position(110)
    @Description("The timestamp when the simulation was started")
    @OrderBy(OrderBy.Direction.DESC)
    @CreatedDate
    @CreatedAt
    private LocalDateTime startedAt;

    @Position(111)
    @Description("The duration of the simulation")
    @Filterable
    private Duration duration;

    @Position(112)
    @Description("The user that triggered the simulation (if not present, it was triggered by a scheduler)")
    private String user;

    @Label("Logs")
    @Column(name = "logs_uri", length = 500)
    @Description("A resource containing the log of the simulation")
    @Align(Align.Type.CENTER)
    @Position(120)
    @Renderable(action = "rest.simulation.view.logs", icon = "fa-regular fa-file-lines")
    private String logsURI;

    @Label("Report")
    @Column(name = "report_uri", length = 500)
    @Description("The resource containing the report of the simulation")
    @Align(Align.Type.CENTER)
    @Position(121)
    @Renderable(action = "rest.simulation.view.report", icon = "fa-solid fa-chart-line")
    private String reportURI;

    @Label("Data")
    @Column(name = "data_uri", length = 500)
    @Description("The resource containing the data produced byt the simulation")
    @Align(Align.Type.CENTER)
    @Position(122)
    @Renderable(action = "rest.simulation.view.data", icon = "fa-solid fa-file-csv")
    private String dataURI;
}
