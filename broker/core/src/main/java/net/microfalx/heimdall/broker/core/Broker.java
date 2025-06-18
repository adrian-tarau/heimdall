package net.microfalx.heimdall.broker.core;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.dataset.annotation.Lookup;
import net.microfalx.bootstrap.dataset.lookup.TimeZoneLookup;
import net.microfalx.bootstrap.jdbc.entity.surrogate.NamedAndTimestampedIdentityAware;
import net.microfalx.lang.annotation.*;

import java.time.ZoneId;

@Entity
@Table(name = "broker_clusters")
@Name("Brokers")
@Getter
@Setter
@ToString(callSuper = true)
public class Broker extends NamedAndTimestampedIdentityAware<Integer> {

    @Column(name = "type")
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
    @Lookup(model = TimeZoneLookup.class)
    private String timeZone = ZoneId.systemDefault().getId();

    @Label("End Points")
    @Transient
    @Position(20)
    @Filterable
    @Visible(modes = Visible.Mode.BROWSE)
    private String endPoints;

    @Column(name = "parameters", length = 100)
    @Position(20)
    @Description("The parameters used to create a broker client (and producers/consumers)")
    @Component(Component.Type.TEXT_AREA)
    @Filterable
    @NotBlank
    @Visible(modes = {Visible.Mode.ADD, Visible.Mode.EDIT, Visible.Mode.VIEW})
    private String parameters;
}
