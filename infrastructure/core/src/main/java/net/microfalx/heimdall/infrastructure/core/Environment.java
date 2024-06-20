package net.microfalx.heimdall.infrastructure.core;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTaggedTimestampAware;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Visible;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(name = "infrastructure_environment")
@Name("Environments")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(callSuper = true)
public class Environment extends NamedAndTaggedTimestampAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    @Visible(value = false)
    private Integer id;

    @NaturalId
    @Column(name = "natural_id", nullable = false)
    @Visible(value = false)
    private String naturalId;

    @Column(name = "attributes", nullable = false)
    @Position(20)
    @Description("A collection of attributes, one per line, separated by '=' associated with an environment")
    @Component(Component.Type.TEXT_AREA)
    @Visible(modes = {Visible.Mode.EDIT, Visible.Mode.ADD, Visible.Mode.VIEW})
    private String attributes;
}
