package net.microfalx.heimdall.rest.core.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Position;

@Entity
@Table(name = "rest_library")
@ToString
@Getter
@Setter
public class RestLibrary extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

    @ManyToOne
    @JoinColumn(name = "project_id")
    @Description("The project repository for the library")
    @Position(2)
    private RestProject project;

    @Column(name = "type",nullable = false)
    @Enumerated(EnumType.STRING)
    @Description("The type of simulation")
    @Position(5)
    private Simulation.Type type;

    @Column(name = "resource",nullable = false,length = 2000)
    @Description("The type of simulation")
    @Position(10)
    private String resource;
}
