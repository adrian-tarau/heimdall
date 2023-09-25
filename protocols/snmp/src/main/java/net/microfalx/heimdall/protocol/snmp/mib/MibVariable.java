package net.microfalx.heimdall.protocol.snmp.mib;

import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.annotation.*;
import org.jsmiparser.smi.SmiPrimitiveType;
import org.jsmiparser.smi.SmiVariable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A MIB variable, part of a module.
 */
@Name("Variables")
@ReadOnly
public class MibVariable implements Identifiable<String>, Nameable, Descriptable {

    @Id
    @Visible(false)
    private final String id;

    @Position(1)
    private final MibModule module;

    @Visible(false)
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
    @Position(10)
    @Name
    public String getName() {
        return variable.getId();
    }

    /**
     * Returns a name which identifies the module and the variable.
     *
     * @return a non-null instance
     */
    @Visible(false)
    public String getFullName() {
        return module.getName() + "::" + getName();
    }

    @Override
    @Position(100)
    @Formattable(maximumLength = 100)
    public String getDescription() {
        return variable.getDescription();
    }

    /**
     * Returns the OID for this variable.
     *
     * @return a non-null instance
     */
    @Position(50)
    public String getOid() {
        return MibUtils.getOid(variable);
    }

    /**
     * Returns the units associated with this variable.
     *
     * @return a non-null instance if there is a unit, null otherwise
     */
    @Position(60)
    public String getUnits() {
        return variable.getUnits();
    }

    /**
     * Returns the (primitive) type of the variable.
     *
     * @return a non-null instance if it is a primitive type, null otherwise
     */
    @Position(70)
    public SmiPrimitiveType getType() {
        return variable.getType() != null ? variable.getType().getPrimitiveType() : null;
    }

    /**
     * Returns whether the variable is of type ENUM.
     *
     * @return {@code true} if enum, {@code false} otherwise
     */
    @Visible(false)
    public boolean isEnum() {
        return getType() == SmiPrimitiveType.ENUM;
    }

    /**
     * Returns whether the variable is of type number.
     *
     * @return {@code true} if number, {@code false} otherwise
     */
    @Visible(false)
    public boolean isNumber() {
        return getType() == SmiPrimitiveType.COUNTER_32 || getType() == SmiPrimitiveType.COUNTER_64 || getType() == SmiPrimitiveType.INTEGER
                || getType() == SmiPrimitiveType.INTEGER_32 || getType() == SmiPrimitiveType.UNSIGNED_32 || getType() == SmiPrimitiveType.GAUGE_32;
    }

    /**
     * Returns the available values for the enum, if the variable has an ENUM type.
     *
     * @return the enum
     */
    @Visible(false)
    public Collection<MibNamedNumber> getEnumValues() {
        if (getType() == SmiPrimitiveType.ENUM) {
            return variable.getType().getNamedNumbers().stream().map(MibNamedNumber::new).toList();
        } else {
            return Collections.emptyList();
        }
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
                ", oid=" + getOid() +
                ", description=" + getDescription() +
                '}';
    }
}
