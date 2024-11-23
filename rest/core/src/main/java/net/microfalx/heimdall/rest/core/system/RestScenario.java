package net.microfalx.heimdall.rest.core.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Position;

@Entity
@Table(name = "rest_scenario")
@ToString
@Getter
@Setter
public class RestScenario extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

    @Column(name = "natural_id", nullable = false,length = 100)
    @Description("The natural key of the scenario")
    @Position(2)
    private String naturalId;

    @ManyToOne
    @JoinColumn(name = "simulation_id", nullable = false)
    @Description("The natural key of the scenario")
    @Position(3)
    private RestSimulation simulation;

    @Column(name = "start_time")
    @Description("The start time for the scenario")
    @Position(10)
    private Integer startTime;

    @Column(name = "gracefulStop")
    @Description("The grace stop of the scenario")
    @Position(15)
    private Integer gracefulStop;

    @Column(name = "function",length = 100)
    @Description("The function")
    @Position(20)
    private String function;
}
