package net.microfalx.heimdall.protocol.snmp.mib;

import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import org.jsmiparser.smi.SmiPrimitiveType;
import org.jsmiparser.smi.SmiVariable;

import java.util.Objects;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A MIB variable, part of a module.
 */
public class MibVariable implements Identifiable<String>, Nameable, Descriptable {

    private final String id;
    private final MibModule module;
    private final SmiVariable variable;

    MibVariable(MibModule module, SmiVariable variable) {
        requireNonNull(module);
        requireNonNull(variable);
        this.module = module;
        this.variable = variable;
        this.id = module.getId() + ":" + toIdentifier(variable.getId());
    }

    /**
     * Returns the module.
     *
     * @return a non-null instance
     */
    public MibModule getModule() {
        return module;
    }

    /**
     * Returns the raw variable (returned by the parser).
     *
     * @return a non-null instance
     */
    public SmiVariable getVariable() {
        return variable;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return variable.getId();
    }

    /**
     * Returns a name which identifies the module and the variable.
     *
     * @return a non-null instance
     */
    public String getFullName() {
        return module.getName() + "::" + getName();
    }

    @Override
    public String getDescription() {
        return variable.getDescription();
    }

    /**
     * Returns the OID for this variable.
     *
     * @return a non-null instance
     */
    public String getOid() {
        return variable.getCodeOid();
    }

    /**
     * Returns the units associated with this variable.
     *
     * @return a non-null instance if there is a unit, null otherwise
     */
    public String getUnits() {
        return variable.getUnits();
    }

    /**
     * Returns the (primitive) type of the variable.
     *
     * @return a non-null instance
     */
    public SmiPrimitiveType getType() {
        return variable.getType().getPrimitiveType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MibVariable that = (MibVariable) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MibVariable{" +
                "id=" + getId() +
                ", oid=" + variable.getOidStr() +
                ", description=" + variable.getDescription() +
                '}';
    }
}
