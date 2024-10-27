package net.microfalx.heimdall.rest.core.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.lang.annotation.*;

@Entity
@Name("Library")
@Table(name = "rest_library")
@ToString
@Getter
@Setter
public class RestLibrary extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

    @Column(name = "natural_id", nullable = false, length = 100, unique = true)
    @NaturalId
    @Visible(value = false)
    @Position(2)
    private String naturalId;

    @ManyToOne
    @JoinColumn(name = "project_id")
    @Description("The project repository for the library")
    @Position(3)
    private RestProject project;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Description("The type of simulation")
    @Position(10)
    private Simulation.Type type;

    @Column(name = "resource", nullable = false, length = 2000)
    @Description("The type of simulation")
    @Position(15)
    private String resource;
}
