package net.microfalx.heimdall.protocol.snmp.mib;

import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import org.jsmiparser.smi.SmiModule;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Objects;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A MIB module.
 */
public class MibModule implements Identifiable<String>, Nameable, Descriptable {

    private final String id;
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

    @Override
    public String getName() {
        return module.getId();
    }

    @Override
    public String getDescription() {
        return module.getModuleIdentity().getDescription();
    }

    /**
     * Returns the OID for this module.
     *
     * @return a non-null instance
     */
    public String getOid() {
        return module.getCodeId();
    }

    /**
     * Returns the organization.
     *
     * @return a non-null instance if the organization is defined, null otherwise
     */
    public String getOrganization() {
        return module.getModuleIdentity() != null ? module.getModuleIdentity().getOrganization() : null;
    }

    /**
     * Returns the contact information.
     *
     * @return a non-null instance if provided, null otherwise
     */
    public String getContactInformation() {
        return module.getModuleIdentity() != null ? module.getModuleIdentity().getContactInfo() : null;
    }

    /**
     * Returns the date/time when the MIB was last modified.
     *
     * @return a non-null instance if provided, null for unknown
     */
    public ZonedDateTime getLastModified() {
        return module.getModuleIdentity() != null ? MibUtils.parseDateTime(module.getModuleIdentity().getLastUpdated()) : null;
    }

    /**
     * Returns all variables defined in the module.
     *
     * @return a non-null instance
     */
    public Collection<MibVariable> getVariables() {
        return module.getVariables().stream().map(v -> new MibVariable(this, v)).toList();
    }

    /**
     * Returns all symbols defined in the module.
     *
     * @return a non-null instance
     */
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
