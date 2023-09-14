package net.microfalx.heimdall.protocol.snmp.mib;

import net.microfalx.lang.StringUtils;
import org.jsmiparser.smi.SmiModule;
import org.jsmiparser.smi.SmiOidValue;
import org.jsmiparser.smi.SmiSymbol;
import org.jsmiparser.util.location.Location;
import org.snmp4j.smi.OID;

import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.stream.Collectors;

import static net.microfalx.resource.ResourceUtils.isFileUri;

/**
 * Utilities around MIBs.
 */
public class MibUtils {

    private static final String INTERNAL_MIB_SOURCE = "JSMI_INTERNAL_MIB";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmX");

    /**
     * Parses a date/time.
     *
     * @param value the date/time as string
     * @return the date/time, null if missing or it cannot be parsed
     */
    public static ZonedDateTime parseDateTime(String value) {
        if (StringUtils.isEmpty(value)) return null;
        try {
            return ZonedDateTime.parse(value, FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns whether the value represents an OID.
     *
     * @param value the value to test
     * @return {@code true} if it is an OID,  {@code false} otherwise
     */
    public static boolean isOid(String value) {
        if (StringUtils.isEmpty(value)) return false;
        for (char c : value.toCharArray()) {
            if (!(Character.isDigit(c) || c == '.')) return false;
        }
        return true;
    }

    /**
     * Returns whether the OID is valid.
     *
     * @param value the value to test
     * @return {@code true} if it is an OID and it is valid,  {@code false} otherwise
     */
    public static boolean isValidOid(String value) {
        if (!isOid(value)) return false;
        OID oid = new OID(value);
        return oid.get(0) > 0;
    }

    /**
     * Returns the parent OID.
     *
     * @param oid the OID
     * @return the parent, null if there is no parent
     */
    public static OID getParent(OID oid) {
        return oid != null ? oid.trim() : null;
    }

    /**
     * Returns whether the URI is valid.
     *
     * @param uri the URI
     * @return {@code true} if valid,  {@code false} otherwise
     */
    public static boolean isMibUri(String uri) {
        try {
            return !INTERNAL_MIB_SOURCE.equals(uri) && isFileUri(URI.create(uri));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns whether the module is valid in the context of our application.
     * <p>
     * A module must have a source URI to be valid.
     *
     * @param module the symbol
     * @return {@code true} if valid, {@code false} otherwise
     */
    public static boolean isValid(SmiModule module) {
        if (module == null) return false;
        Location location = module.getIdToken().getLocation();
        return location != null && (MibUtils.isMibUri(location.getSource()));
    }

    /**
     * Returns whether the symbol is valid in the context of our application.
     *
     * @param symbol the symbol
     * @return {@code true} if valid, {@code false} otherwise
     */
    public static boolean isValid(SmiSymbol symbol) {
        if (symbol == null) return false;
        if (symbol instanceof SmiOidValue) {
            return ((SmiOidValue) symbol).getNode() != null;
        } else {
            return true;
        }
    }

    /**
     * Returns a collection of variables which have valid OIDs.
     *
     * @param variables a collection with variables
     * @return a collection with variables which they have OIDs
     */
    public static Collection<MibVariable> getValid(Collection<MibVariable> variables) {
        return variables.stream().filter(v -> v.getOid() != null).collect(Collectors.toList());
    }


    /**
     * Returns the OID from a symbol.
     *
     * @param value the value
     * @return the OID if symbol has an OID or it could be resolved, null otherwise
     */
    public static String getOid(SmiSymbol value) {
        return value instanceof SmiOidValue && ((SmiOidValue) value).getNode() != null
                ? ((SmiOidValue) value).getNode().getOidStr() : null;
    }
}
