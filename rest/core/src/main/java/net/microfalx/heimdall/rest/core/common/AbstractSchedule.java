package net.microfalx.heimdall.rest.core.common;

import jakarta.persistence.Id;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.dataset.annotation.Tab;
import net.microfalx.bootstrap.dataset.annotation.Tabs;
import net.microfalx.bootstrap.jdbc.entity.TimestampAware;
import net.microfalx.heimdall.infrastructure.core.system.Environment;
import net.microfalx.heimdall.rest.core.system.RestSimulation;
import net.microfalx.lang.annotation.*;

import java.time.LocalDateTime;

@MappedSuperclass
@ToString
@Getter
@Setter
@Tabs
public abstract class AbstractSchedule extends TimestampAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Position(1)
    @Visible(false)
    private int id;

    @ManyToOne
    @JoinColumn(name = "environment_id", nullable = false)
    @Description("The environment on which the simulation will be executed")
    @Name
    @Position(10)
    private Environment environment;

    @ManyToOne
    @JoinColumn(name = "simulation_id", nullable = false)
    @Description("The simulation to be executed by this schedule")
    @Name
    @Position(15)
    private RestSimulation simulation;

    @Column(name = "active")
    @Description("Indicates whether a schedule is enabled")
    @Position(16)
    private boolean active = true;

    @Column(name = "type", length = 100, nullable = false)
    @Enumerated(EnumType.STRING)
    @Description("The type of the scheduler")
    @Position(20)
    private net.microfalx.heimdall.rest.api.Schedule.Type type;

    @Column(name = "expression", length = 100)
    @Description("The scheduling expression")
    @Position(25)
    @Filterable
    private String expression;

    @Column(name = "interval", length = 100)
    @Description("The scheduling interval")
    @Position(30)
    @Filterable
    private String interval;

    @Column(name = "vus")
    @Description("The number of virtual users")
    @Position(40)
    @Visible(modes = {Visible.Mode.ADD, Visible.Mode.EDIT})
    @Tab(label = "Options")
    private Integer vus;

    @Column(name = "duration",length = 50)
    @Description("The duration")
    @Position(45)
    @Visible(modes = {Visible.Mode.ADD, Visible.Mode.EDIT})
    @Tab(label = "Options")
    private String duration;

    @Column(name = "iterations")
    @Description("The iterations")
    @Position(50)
    @Visible(modes = {Visible.Mode.ADD, Visible.Mode.EDIT})
    @Tab(label = "Options")
    private Integer iterations;

    @Column(name = "attributes",length = 4000)
    @Description("The attributes")
    @Position(55)
    @Visible(modes = {Visible.Mode.ADD, Visible.Mode.EDIT})
    @Component(Component.Type.TEXT_AREA)
    @Tab(label = "Options")
    private String attributes;

    @Transient
    @Description("The timestamp when the next simulation will be triggered")
    @Position(32)
    @Visible(modes = Visible.Mode.BROWSE)
    private LocalDateTime nextRunAt;

    @Column(name = "description")
    @Position(1000)
    @Component(Component.Type.TEXT_AREA)
    @Description("A description for a {name}")
    @Width("300px")
    @Filterable()
    private String description;
}
