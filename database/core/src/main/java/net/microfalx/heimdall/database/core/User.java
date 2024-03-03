package net.microfalx.heimdall.database.core;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedTimestampAware;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.ReadOnly;

@Entity(name = "DatabaseUser")
@Table(name = "database_users")
@Name("Users")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(callSuper = true)
@ReadOnly
public class User extends NamedTimestampAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Integer id;

}
