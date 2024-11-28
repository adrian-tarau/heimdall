package net.microfalx.heimdall.rest.core.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.heimdall.rest.api.Project;
import net.microfalx.lang.annotation.*;

@Entity
@Name("Project")
@Table(name = "rest_project")
@ToString
@Getter
@Setter
public class RestProject extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

    @Column(name = "natural_id", nullable = false, length = 100, unique = true)
    @NaturalId
    @Visible(value = false)
    private String naturalId;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Description("The type of project")
    @Position(10)
    private Project.Type type;

    @Column(name = "uri", length = 2000)
    @Description("The URI to the project repository")
    @Position(15)
    @Filterable
    @Formattable(maximumLength = 40)
    private String uri;

    @Column(name = "user_name", length = 100)
    @Description("The username to the project repository")
    @Position(20)
    @Visible(modes = {Visible.Mode.ADD, Visible.Mode.EDIT})
    @Filterable
    private String userName;

    @Column(name = "password", length = 100)
    @Description("The password to the project repository")
    @Position(25)
    @Visible(modes = {Visible.Mode.ADD, Visible.Mode.EDIT})
    @Component(Component.Type.PASSWORD)
    @Filterable
    private String password;

    @Column(name = "token", length = 2000)
    @Description("The token to the project repository")
    @Position(30)
    @Visible(modes = {Visible.Mode.ADD, Visible.Mode.EDIT})
    @Component(Component.Type.PASSWORD)
    @Filterable
    private String token;

    @Column(name = "simulation_path", length = 2000)
    @Description("The path within the project which holds the simulations")
    @Position(40)
    @Label(group = "Paths", value = "Simulation")
    @Formattable(maximumLength = 30)
    @Filterable
    private String simulationPath;

    @Column(name = "library_path", length = 2000)
    @Description("The path within the project which holds the (shared) libraries required for the simulation")
    @Position(45)
    @Formattable(maximumLength = 30)
    @Label(group = "Paths", value = "Library")
    @Filterable
    private String libraryPath;


}
