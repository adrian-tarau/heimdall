package net.microfalx.heimdall.protocol.snmp.mib;

import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.annotation.*;
import org.jsmiparser.smi.SmiOidValue;
import org.jsmiparser.smi.SmiSymbol;
import org.jsmiparser.smi.SmiTextualConvention;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A MIB symbol.
 */
@Name("Symbols")
@ReadOnly
public class MibSymbol implements Identifiable<String>, Nameable, Descriptable {

    @Id
    @Visible(false)
    private final String id;

    @Position(1)
    private final MibModule module;

    @Visible(false)
    private final SmiSymbol symbol;

    public MibSymbol(MibModule module, SmiSymbol symbol) {
        requireNonNull(module);
        requireNonNull(symbol);
        this.module = module;
        this.symbol = symbol;
        this.id = module.getId() + ":" + toIdentifier(symbol.getId());
    }

    /**
     * Returns the module.
     *
     * @return a non-null instance
     */
    public MibModule getModule() {
        return module;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    @Position(10)
    @Name
    public String getName() {
        return symbol.getId();
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

    @Position(100)
    @Formattable(maximumLength = 100)
    @Override
    public String getDescription() {
        if (symbol instanceof SmiTextualConvention) {
            return ((SmiTextualConvention) symbol).getDescription();
        } else {
            return null;
        }
    }

    /**
     * Returns the OID for this symbol.
     *
     * @return a non-null instance if the symbol has an OID, null otherwise
     */
    @Position(50)
    public String getOid() {
        return symbol instanceof SmiOidValue ? ((SmiOidValue) symbol).getNode().getOidStr() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MibSymbol mibSymbol)) return false;

        return id.equals(mibSymbol.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "MibSymbol{" +
                "id='" + id + '\'' +
                ", module=" + module +
                ", symbol=" + symbol +
                '}';
    }
}
