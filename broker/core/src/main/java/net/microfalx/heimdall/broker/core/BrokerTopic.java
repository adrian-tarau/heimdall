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
@Table(name = "broker_topics")
@Name("Topics")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(callSuper = true)
public class BrokerTopic extends NamedTimestampAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "cluster_id", nullable = false)
    @Position(2)
    @Label(value = "Name", group = "Broker")
    @Description("The broker which owns the topic")
    private Broker broker;

    @Column(name = "type")
    @Position(6)
    @Enumerated(EnumType.STRING)
    @Width(min = "50")
    @Description("The type of broker")
    @NotNull
    @Visible(modes = {Visible.Mode.BROWSE})
    private net.microfalx.bootstrap.broker.Broker.Type type;

    @Column(name = "active")
    @Position(10)
    @Description("Indicates whether the topic is active (events will be pulled)")
    private boolean active;

    @Column(name = "sample_size")
    @Position(11)
    @Description("The size of the event sample (1 in N events")
    private Integer sampleSize;

    @Column(name = "mime_type")
    @Position(12)
    @Description("The mime type of the event (how it is the event encoded)")
    @Visible(modes = {Visible.Mode.BROWSE})
    private String mimeType;

    @Column(name = "name_expression")
    @Position(20)
    @Description("An MVEL expression used to extract the event name from event attributes")
    @Visible(modes = {Visible.Mode.ADD, Visible.Mode.EDIT})
    @Component(Component.Type.TEXT_AREA)
    private String nameExpression;

    @Column(name = "description_expression")
    @Position(20)
    @Description("An MVEL expression used to extract the event description from event attributes")
    @Visible(modes = {Visible.Mode.ADD, Visible.Mode.EDIT})
    @Component(Component.Type.TEXT_AREA)
    private String descriptionExpression;

    @Column(name = "attribute_inclusions")
    @Position(30)
    @Description("An comma separate list of regular expressions used to select which event attributes to be indexed (if empty all will be included)")
    @Visible(modes = {Visible.Mode.ADD, Visible.Mode.EDIT})
    @Component(Component.Type.TEXT_AREA)
    private String attributeInclusions;

    @Column(name = "attribute_exclusions")
    @Position(31)
    @Description("An comma separate list of regular expressions used to select which event attributes to be excluded (not indexed)")
    @Visible(modes = {Visible.Mode.ADD, Visible.Mode.EDIT})
    @Component(Component.Type.TEXT_AREA)
    private String attributeExclusions;

    @Column(name = "attribute_prefixes")
    @Position(32)
    @Description("An comma separate list of attribute prefixes to be removed")
    @Visible(modes = {Visible.Mode.ADD, Visible.Mode.EDIT})
    @Component(Component.Type.TEXT_AREA)
    private String attributePrefixes;

    @Column(name = "parameters")
    @Position(100)
    @Description("The parameters used to create a producers/consumers, on top of broker parameters (additional parameters specific to a topic)")
    @Component(Component.Type.TEXT_AREA)
    @Filterable
    @NotBlank
    @Visible(modes = {Visible.Mode.ADD, Visible.Mode.EDIT, Visible.Mode.VIEW})
    private String parameters;
}
