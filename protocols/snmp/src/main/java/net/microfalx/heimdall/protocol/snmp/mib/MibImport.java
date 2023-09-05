package net.microfalx.heimdall.protocol.snmp.mib;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import org.jsmiparser.smi.SmiImports;

import java.util.Collection;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * Holds a reference to an import.
 */
public class MibImport implements Identifiable<String>, Nameable {

    private final MibModule module;
    private final String id;
    private final String idToken;
    private final SmiImports smiImport;

    private Collection<MibSymbol> symbols;

    MibImport(MibModule module, SmiImports smiImport) {
        requireNonNull(module);
        requireNonNull(smiImport);
        this.module = module;
        this.smiImport = smiImport;
        this.idToken = smiImport.getModuleToken().getValue();
        this.id = toIdentifier(this.idToken);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return idToken;
    }

    /**
     * Returns the symbols referenced by this import.
     *
     * @return a non-null instance
     */
    public Collection<MibSymbol> getSymbols() {
        if (symbols == null) {
            symbols = smiImport.getSymbols().stream().map(symbol -> new MibSymbol(module, symbol)).toList();
        }
        return symbols;
    }

    /**
     * Returns the MIB token ID for this module (what comes before <code>DEFINITIONS ::= BEGIN</code>).
     *
     * @return a non-null instance
     */
    public String getIdToken() {
        return idToken;
    }

    @Override
    public String toString() {
        return "MibImport{" +
                "id='" + id + '\'' +
                '}';
    }
}
