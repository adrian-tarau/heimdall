package net.microfalx.heimdall.infrastructure.core.overview;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.model.NamedIdentityAware;
import net.microfalx.lang.annotation.Name;

@Name("Clusters")
@Getter
@Setter
@ToString(callSuper = true)
public class Cluster extends NamedIdentityAware<String> {

}
