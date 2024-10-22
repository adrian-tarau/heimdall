package net.microfalx.heimdall.rest.core.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.jdbc.entity.TimestampAware;
import net.microfalx.heimdall.infrastructure.core.system.Environment;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Visible;
import net.microfalx.lang.annotation.Width;

@Entity
@Table(name = "rest_schedule")
@ToString
@Getter
@Setter
public class Schedule extends TimestampAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Position(1)
    @Visible(false)
    private int id;

    @ManyToOne
    @JoinColumn(name = "environment_id", nullable = false)
    @Description("The environment on which the simulation will be executed")
    @Position(10)
    private Environment environment;

    @ManyToOne
    @JoinColumn(name = "simulation_id", nullable = false)
    @Description("The simulation to be executed by this schedule")
    @Position(15)
    private Simulation simulation;

    @Column(name = "expression", length = 100)
    @Description("The scheduling expression")
    @Position(20)
    private String expression;

    @Column(name = "interval")
    @Description("The scheduling interval")
    @Position(25)
    private String interval;

    @Column(name = "description")
    @Position(1000)
    @Component(Component.Type.TEXT_AREA)
    @Description("A description for a {name}")
    @Width("300px")
    @Filterable()
    private String description;
}
