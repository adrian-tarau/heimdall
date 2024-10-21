package net.microfalx.heimdall.rest.core.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.NaturalId;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Visible;

@Entity
@Table(name = "rest_simulation")
@ToString
@Getter
@Setter
public class Simulation extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

    @Column(name = "natural_id",nullable = false,length = 100,unique = true)
    @NaturalId
    @Description("The natural id of the simulation")
    @Visible(modes = {Visible.Mode.BROWSE,Visible.Mode.VIEW})
    @Position(2)
    private String naturalId;

    @Column(name = "type",nullable = false)
    @Enumerated(EnumType.STRING)
    @Description("The type of the simulation")
    @Position(50)
    private net.microfalx.heimdall.rest.api.Simulation.Type type;

    @Column(name = "resource",nullable = false,length = 1000)
    @Description("The resource")
    @Position(100)
    private String resource;
}
