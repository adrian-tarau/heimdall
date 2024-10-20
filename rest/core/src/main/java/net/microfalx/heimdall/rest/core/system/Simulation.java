package net.microfalx.heimdall.rest.core.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.lang.annotation.NaturalId;

@Entity
@Table(name = "rest_simulation")
@ToString
@Getter
@Setter
public class Simulation extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

    @Column(name = "natural_id",nullable = false,length = 100,unique = true)
    @NaturalId
    private Integer naturalId;

    @Column(name = "type",nullable = false)
    @Enumerated(EnumType.STRING)
    private net.microfalx.heimdall.rest.api.Simulation.Type type;

    @Column(name = "resource",nullable = false,length = 1000)
    private String resource;
}
