package net.microfalx.heimdall.infrastructure.core.overview;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.dataset.model.NamedIdentityAware;
import net.microfalx.heimdall.infrastructure.api.HealthAware;
import net.microfalx.lang.annotation.Visible;

/**
 * Base class for all models used to support dashboards for infrastructure elements.
 */
@Getter
@Setter
public abstract class InfrastructureElement extends NamedIdentityAware<String> implements HealthAware, net.microfalx.heimdall.infrastructure.api.InfrastructureElement {

    /**
     * Returns the original infrastructure element.
     */
    @Visible(value = false)
    private net.microfalx.heimdall.infrastructure.api.InfrastructureElement reference;
}
