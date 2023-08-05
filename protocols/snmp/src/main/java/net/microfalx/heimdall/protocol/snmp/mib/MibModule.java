package net.microfalx.heimdall.protocol.snmp.mib;

import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.annotation.*;
import org.jsmiparser.smi.SmiModule;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Objects;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A MIB module.
 */
@Name("Module")
@ReadOnly
public class MibModule implements Identifiable<String>, Nameable, Descriptable {

    @Id
    @Visible(false)
    private final String id;

    @Visible(false)
    private final SmiModule module;

    MibModule(SmiModule module) {
        requireNonNull(module);
        this.module = module;
        this.id = toIdentifier(module.getId());
    }

    /**
     * Returns the raw module (returned by the parser).
     *
     * @return a non-null instance
     */
    public SmiModule getModule() {
        return module;
    }

    @Override

    public String getId() {
        return id;
    }

    @Position(1)
    @Override
    @Name
    public String getName() {
        return module.getId();
    }

    @Override
    @Position(100)
    @Formattable(maximumLines = 2)
    public String getDescription() {
        return module.getModuleIdentity() != null ? module.getModuleIdentity().getDescription() : null;
    }

    /**
     * Returns the OID for this module.
     *
     * @return a non-null instance
     */
    @Visible(false)
    public String getOid() {
        return module.getCodeId();
    }

    /**
     * Returns the organization.
     *
     * @return a non-null instance if the organization is defined, null otherwise
     */
    @Position(20)
    public String getOrganization() {
        return module.getModuleIdentity() != null ? module.getModuleIdentity().getOrganization() : null;
    }

    /**
     * Returns the contact information.
     *
     * @return a non-null instance if provided, null otherwise
     */
    @Visible(false)
    public String getContactInformation() {
        return module.getModuleIdentity() != null ? module.getModuleIdentity().getContactInfo() : null;
    }

    /**
     * Returns the date/time when the MIB was last modified.
     *
     * @return a non-null instance if provided, null for unknown
     */
    @Position(110)
    public ZonedDateTime getLastModified() {
        return module.getModuleIdentity() != null ? MibUtils.parseDateTime(module.getModuleIdentity().getLastUpdated()) : null;
    }

    /**
     * Returns all variables defined in the module.
     *
     * @return a non-null instance
     */
    @Visible(false)
    public Collection<MibVariable> getVariables() {
        return module.getVariables().stream().map(v -> new MibVariable(this, v)).toList();
    }

    /**
     * Returns all symbols defined in the module.
     *
     * @return a non-null instance
     */
    @Visible(false)
    public Collection<MibSymbol> getSymbols() {
        return module.getSymbols().stream().map(s -> new MibSymbol(this, s)).toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MibModule module = (MibModule) o;
        return Objects.equals(id, module.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MibModule{" +
                "id=" + getId() +
                ", identify=" + module.getModuleIdentity() +
                '}';
    }
}
