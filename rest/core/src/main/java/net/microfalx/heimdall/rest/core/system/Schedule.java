package net.microfalx.heimdall.rest.core.system;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTimestampedIdentityAware;
import net.microfalx.heimdall.infrastructure.core.system.Environment;

@Entity
@Table(name = "rest_schedule")
@ToString
@Getter
@Setter
public class Schedule extends NamedAndTimestampedIdentityAware<Integer> {

    @JoinColumn(name = "environment_id", nullable = false)
    private Environment environment;

    @JoinColumn(name = "simulation_id", nullable = false)
    private Simulation simulation;

    @Column(name = "expression", nullable = false, length = 100)
    private String expression;

    @Column(name = "interval")
    private String interval;
}
