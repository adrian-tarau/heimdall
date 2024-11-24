package net.microfalx.heimdall.rest.core.system;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.heimdall.rest.core.common.AbstractLibrary;
import net.microfalx.lang.annotation.Name;

@Entity
@Name("Library")
@Table(name = "rest_library")
@ToString
@Getter
@Setter
public class RestLibrary extends AbstractLibrary {
}
