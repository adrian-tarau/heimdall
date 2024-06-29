package net.microfalx.heimdall.database.core;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTimestampedIdentityAware;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.ReadOnly;

@Entity(name = "DatabaseUser")
@Table(name = "database_users")
@Name("Users")
@Getter
@Setter
@ToString(callSuper = true)
@ReadOnly
public class User extends NamedAndTimestampedIdentityAware<Integer> {


}
