package net.microfalx.heimdall.protocol.snmp.mib;

import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.annotation.*;
import org.jsmiparser.smi.*;

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

    /**
     * Returns the type of the symbol.
     *
     * @return a non-null instance
     */
    @Position(20)
    public SymbolType getType() {
        if (symbol instanceof SmiVariable) {
            return SymbolType.VARIABLE;
        } else if (symbol instanceof SmiTrapType) {
            return SymbolType.TRAP_TYPE;
        } else if (symbol instanceof SmiRow) {
            return SymbolType.ROW;
        } else if (symbol instanceof SmiTable) {
            return SymbolType.TABLE;
        } else if (symbol instanceof SmiReferencedType) {
            return SymbolType.REFERENCED_TYPE;
        } else if (symbol instanceof SmiTextualConvention) {
            return SymbolType.TEXTUAL_CONVENTION;
        } else if (symbol instanceof SmiProtocolType) {
            return SymbolType.PROTOCOL_TYPE;
        } else if (symbol instanceof SmiMacro) {
            return SymbolType.MACRO;
        } else if (symbol instanceof SmiType) {
            return SymbolType.TYPE;
        } else {
            return SymbolType.OTHER;
        }
    }

    /**
     * Returns the (primitive) type of the variable.
     *
     * @return a non-null instance if it is a primitive type, null otherwise
     */
    @Position(30)
    public SmiPrimitiveType getPrimitiveType() {
        return symbol instanceof SmiType ? ((SmiType) symbol).getPrimitiveType() : null;
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
        return MibUtils.getOid(symbol);
    }

    /**
     * Returns whether the symbol is actually a variable.
     *
     * @return {@code true} if it is a variable, {@code false} otherwise
     */
    @Visible(false)
    public boolean isVariable() {
        return symbol instanceof SmiVariable;
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
