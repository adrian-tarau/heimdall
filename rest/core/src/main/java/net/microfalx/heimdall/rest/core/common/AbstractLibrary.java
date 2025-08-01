package net.microfalx.heimdall.rest.core.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.Tabs;
import net.microfalx.bootstrap.jdbc.entity.surrogate.NamedAndOwnedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.core.system.RestProject;
import net.microfalx.lang.annotation.*;

@MappedSuperclass
@ToString
@Getter
@Setter
@Tabs(attributes = {"tags", "description"})
public abstract class AbstractLibrary extends NamedAndOwnedAndTaggedAndTimestampedIdentityAware<Integer> {

    @Column(name = "natural_id", nullable = false, length = 100, unique = true)
    @NaturalId
    @Visible(value = false)
    @Position(2)
    private String naturalId;

    @ManyToOne
    @JoinColumn(name = "project_id")
    @Description("The project repository")
    @Position(3)
    @ReadOnly(modes = ReadOnly.Mode.EDIT)
    private RestProject project;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Description("The type of simulation")
    @Position(10)
    @ReadOnly(modes = ReadOnly.Mode.EDIT)
    private Simulation.Type type;

    @Column(name = "path", nullable = false, length = 2000)
    @Description("The path of the script")
    @Formattable(maximumLength = 40)
    @Position(15)
    @ReadOnly(modes = ReadOnly.Mode.EDIT)
    @Filterable
    private String path;

    @Column(name = "override", nullable = false)
    @Description("Indicates whether the content was changed from UI. Any update from repository is canceled until the override is removed")
    @Position(16)
    private boolean override;

    @Column(name = "resource", nullable = false, length = 2000)
    @Visible(false)
    private String resource;

    @Column(name = "hash")
    @Visible(value = false)
    private String hash;

    @Column(name = "version")
    @Description("The version of the library")
    @Position(30)
    @Visible(modes = Visible.Mode.BROWSE)
    private Integer version;
}
