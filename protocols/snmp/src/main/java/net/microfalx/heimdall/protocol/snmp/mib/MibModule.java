package net.microfalx.heimdall.protocol.snmp.mib;

import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import org.jsmiparser.smi.SmiModule;

import java.time.ZonedDateTime;
import java.util.Collection;

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
        return module.getCodeId();
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
     * @return a non-null instance
     */
    public String getOrganization() {
        return module.getModuleIdentity().getOrganization();
    }

    /**
     * Returns the contact information.
     *
     * @return a non-null instance if provided, null otherwise
     */
    public String getContactInformation() {
        return module.getModuleIdentity().getContactInfo();
    }

    /**
     * Returns the date/time when the MIB was last modified.
     *
     * @return a non-null instance if provided, null for unknown
     */
    public ZonedDateTime getLastModified() {
        return MibUtils.parseDateTime(module.getModuleIdentity().getLastUpdated());
    }

    /**
     * Returns all variables defined in the module.
     *
     * @return a non-null instance
     */
    public Collection<MibVariable> getVariables() {
        return module.getVariables().stream().map(smiVariable -> new MibVariable(this, smiVariable)).toList();
    }

    @Override
    public String toString() {
        return "MibModule{" +
                "module=" + module +
                '}';
    }
}
