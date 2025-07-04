package net.microfalx.heimdall.broker.core;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.broker.Topic;
import net.microfalx.bootstrap.dataset.Alert;
import net.microfalx.bootstrap.dataset.annotation.*;
import net.microfalx.bootstrap.jdbc.entity.surrogate.NamedAndTimestampedIdentityAware;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.annotation.*;
import net.microfalx.resource.MimeType;

@Entity
@Table(name = "broker_topics")
@Name("Topics")
@Getter
@Setter
@ToString(callSuper = true)
@Tabs
public class BrokerTopic extends NamedAndTimestampedIdentityAware<Integer> {

    @ManyToOne
    @JoinColumn(name = "cluster_id", nullable = false)
    @Position(2)
    @Description("The broker which owns the topic")
    @Visible(modes = {Visible.Mode.ADD, Visible.Mode.BROWSE})
    @NotNull
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
    @Visible(modes = {Visible.Mode.BROWSE, Visible.Mode.EDIT})
    private Integer sampleSize;

    @Column(name = "mime_type")
    @Position(12)
    @Description("The mime type of the event (how it is the event encoded)")
    @Visible(modes = {Visible.Mode.BROWSE})
    private String mimeType = MimeType.APPLICATION_OCTET_STREAM.toString();

    @Transient
    @Position(15)
    @Formattable(alert = AlertProvider.class)
    @Visible(modes = Visible.Mode.BROWSE)
    private Topic.Status status = Topic.Status.HEALTHY;

    @Column(name = "name_expression")
    @Position(20)
    @Description("An MVEL expression used to extract the event name from event attributes")
    @Visible(modes = {Visible.Mode.EDIT, Visible.Mode.VIEW})
    @Component(Component.Type.TEXT_AREA)
    @Tab(label = "Expressions")
    private String nameExpression;

    @Column(name = "description_expression")
    @Position(21)
    @Description("An MVEL expression used to extract the event description from event attributes")
    @Visible(modes = {Visible.Mode.EDIT, Visible.Mode.VIEW})
    @Component(Component.Type.TEXT_AREA)
    @Tab(label = "Expressions")
    private String descriptionExpression;

    @Column(name = "attribute_inclusions")
    @Position(30)
    @Description("An comma separate list of regular expressions used to select which event attributes to be indexed (if empty all will be included)")
    @Visible(modes = {Visible.Mode.EDIT, Visible.Mode.VIEW})
    @Component(Component.Type.TEXT_AREA)
    @Tab(label = "Attributes")
    private String attributeInclusions;

    @Column(name = "attribute_exclusions")
    @Position(31)
    @Description("An comma separate list of regular expressions used to select which event attributes to be excluded (not indexed)")
    @Visible(modes = {Visible.Mode.EDIT, Visible.Mode.VIEW})
    @Component(Component.Type.TEXT_AREA)
    @Tab(label = "Attributes")
    private String attributeExclusions;

    @Column(name = "attribute_prefixes")
    @Position(32)
    @Description("An comma separate list of attribute prefixes to be removed")
    @Visible(modes = {Visible.Mode.EDIT, Visible.Mode.VIEW})
    @Component(Component.Type.TEXT_AREA)
    @Tab(label = "Attributes")
    private String attributePrefixes;

    @Column(name = "parameters")
    @Position(100)
    @Description("The parameters used to create a producers/consumers, on top of broker parameters (additional parameters specific to a topic)")
    @Component(Component.Type.TEXT_AREA)
    @Filterable
    @Visible(value = false)
    private String parameters;

    @Transient
    @Visible(value = false)
    private String lastError;

    public static class AlertProvider implements Formattable.AlertProvider<BrokerTopic, Field<BrokerTopic>, Topic.Status> {

        @Override
        public Alert provide(Topic.Status value, Field<BrokerTopic> field, BrokerTopic model) {
            Alert.Type type = switch (value) {
                case HEALTHY -> Alert.Type.SUCCESS;
                case LATE -> Alert.Type.WARNING;
                case FAULTY -> Alert.Type.DANGER;
            };
            return Alert.builder().type(type).message(model.getLastError()).build();
        }
    }
}
