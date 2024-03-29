package net.microfalx.heimdall.broker.core;

import jakarta.persistence.Id;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.jdbc.entity.NamedTimestampAware;
import net.microfalx.lang.annotation.*;

@Entity
@Table(name = "broker_clusters")
@Name("Brokers")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(callSuper = true)
public class Broker extends NamedTimestampAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "type", length = 500)
    @Position(6)
    @Enumerated(EnumType.STRING)
    @Width(min = "50")
    @Description("The type of broker")
    @NotNull
    private net.microfalx.bootstrap.broker.Broker.Type type;

    @Column(name = "time_zone", length = 100)
    @Position(10)
    @Description("The time zone of the database")
    @NotBlank
    private String timeZone;

    @Column(name = "parameters", length = 100)
    @Position(20)
    @Description("The parameters used to create a broker client (and producers/consumers)")
    @Component(Component.Type.TEXT_AREA)
    @Filterable
    @NotBlank
    @Visible(modes = {Visible.Mode.ADD, Visible.Mode.EDIT, Visible.Mode.VIEW})
    private String parameters;
}
