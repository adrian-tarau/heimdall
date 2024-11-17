package net.microfalx.heimdall.rest.core.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
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

    @Column(name = "path", nullable = false, length = 2000)
    @Description("The path of the script for the library")
    @Formattable(maximumLength = 40)
    @Position(15)
    private String path;

    @Column(name = "override", nullable = false)
    @Description("Indicates whether the library was overwritten from UI")
    @Position(16)
    private boolean override;

    @Column(name = "resource", nullable = false, length = 2000)
    @Visible(false)
    private String resource;

    @Column(name = "hash")
    @Visible(value = false)
    private String hash;
}
