package net.microfalx.heimdall.rest.core.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.lang.annotation.*;

@Entity
@Table(name = "rest_simulation")
@Name("Simulation")
@ToString
@Getter
@Setter
public class RestSimulation extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

    @ManyToOne
    @JoinColumn(name = "project_id")
    @Description("The project repository")
    @Position(2)
    private RestProject project;

    @Column(name = "natural_id", nullable = false, length = 100, unique = true)
    @NaturalId
    @Visible(value = false)
    @Position(3)
    private String naturalId;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Description("The type of the simulation")
    @Position(50)
    private net.microfalx.heimdall.rest.api.Simulation.Type type;

    @Column(name = "path", nullable = false, length = 2000)
    @Description("The path of the script for the simulation")
    @Formattable(maximumLength = 40)
    @Position(15)
    private String path;

    @Column(name = "override", nullable = false)
    @Description("Indicates whether the simulation was overwritten from UI")
    @Position(16)
    private boolean override;

    @Column(name = "timeout", nullable = false)
    @Description("The timeout associated with the simulation")
    @Position(7)
    @Formattable(unit = Formattable.Unit.SECOND)
    private int timeout;

    @Column(name = "resource", nullable = false, length = 1000)
    @Visible(false)
    private String resource;

    @Column(name = "hash")
    @Visible(value = false)
    private String hash;
}
