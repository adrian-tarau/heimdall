package net.microfalx.heimdall.database.core;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.Alert;
import net.microfalx.bootstrap.dataset.annotation.*;
import net.microfalx.bootstrap.dataset.lookup.TimeZoneLookup;
import net.microfalx.bootstrap.jdbc.entity.surrogate.NamedAndTimestampedIdentityAware;
import net.microfalx.bootstrap.jdbc.jpa.EncryptAttributeConverter;
import net.microfalx.bootstrap.jdbc.support.Node;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.annotation.*;

import java.time.ZoneId;

@Entity
@Table(name = "database_schemas")
@Name("Databases")
@Getter
@Setter
@ToString(callSuper = true)
@Tabs
public class Schema extends NamedAndTimestampedIdentityAware<Integer> {

    @Column(name = "type")
    @Position(10)
    @Enumerated(EnumType.STRING)
    @Width(min = "50")
    @Description("The type of database")
    @NotNull
    private net.microfalx.bootstrap.jdbc.support.Database.Type type;

    @Column(name = "url", length = 2000)
    @Position(20)
    @Width(min = "300")
    @Description("The JDBC URL of the database")
    @NotEmpty
    @Tab(label = "Connection")
    private String url;

    @Column(name = "username", length = 100)
    @Position(30)
    @Description("The user name used to connect to the database")
    @Label("User Name")
    @NotEmpty
    @Tab(label = "Connection")
    private String username;

    @Column(name = "password", length = 100)
    @Position(31)
    @Description("The password used to connect to the database")
    @Visible(modes = {Visible.Mode.ADD, Visible.Mode.EDIT})
    @Component(Component.Type.PASSWORD)
    @Convert(converter = EncryptAttributeConverter.class)
    @Tab(label = "Connection")
    private String password;

    @Column(name = "time_zone", length = 100)
    @Position(32)
    @Description("The time zone of the database")
    @Lookup(model = TimeZoneLookup.class)
    private String timeZone = ZoneId.systemDefault().getId();

    @Position(40)
    @Label("State")
    @Description("The state of the database")
    @Visible(modes = Visible.Mode.BROWSE)
    @Formattable(alert = AlertProvider.class)
    @Transient
    private Node.State state = Node.State.UNKNOWN;

    @Column(name = "mappings", length = 2000)
    @Position(40)
    @Description("The mappings used to translate IPs or any additional database specific features")
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.ADD, Visible.Mode.EDIT})
    @Component(Component.Type.TEXT_AREA)
    @Tab(label = "Options")
    private String mappings;

    @Position(2000)
    @Visible(value = false)
    @Transient
    private String validationError;

    public static class AlertProvider implements Formattable.AlertProvider<Schema, Field<Schema>, Node.State> {

        @Override
        public Alert provide(Node.State value, Field<Schema> field, Schema model) {
            Alert.Type type = switch (value) {
                case UP -> Alert.Type.SUCCESS;
                case DOWN -> Alert.Type.DANGER;
                case STANDBY, RECOVERING -> Alert.Type.SECONDARY;
                case UNKNOWN -> Alert.Type.LIGHT;
            };
            return Alert.builder().type(type).message(model.getValidationError()).build();
        }
    }
}
