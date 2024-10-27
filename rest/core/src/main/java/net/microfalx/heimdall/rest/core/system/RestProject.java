package net.microfalx.heimdall.rest.core.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.heimdall.rest.api.Project;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.NaturalId;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Visible;

@Entity
@Table(name = "rest_project")
@ToString
@Getter
@Setter
public class RestProject extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

    @Column(name = "natural_id", nullable = false, length = 100, unique = true)
    @NaturalId
    @Visible(value = false)
    @Position(3)
    private String naturalId;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Description("The type of project")
    @Position(10)
    private Project.Type type;

    @Column(name = "uri", nullable = false, length = 2000)
    @Description("The URI to the project repository")
    @Position(15)
    private String uri;

    @Column(name = "user_name", length = 100)
    @Description("The username to the project repository")
    @Position(20)
    private String userName;

    @Column(name = "password", length = 100)
    @Description("The password to the project repository")
    @Position(25)
    private String password;

    @Column(name = "token", length = 2000)
    @Description("The token to the project repository")
    @Position(30)
    private String token;
}
