package net.microfalx.heimdall.rest.core.system;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.heimdall.rest.core.common.AbstractResult;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.ReadOnly;

@Entity
@Table(name = "rest_result")
@Name("Results")
@ReadOnly
@Getter
@Setter
@ToString
public class RestResult extends AbstractResult {

}
