package net.microfalx.heimdall.protocol.snmp.mib;

import net.microfalx.lang.StringUtils;
import org.snmp4j.smi.OID;

import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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
     * Returns the parent OID.
     *
     * @param oid the OID
     * @return the parent, null if there is no parent
     */
    public static OID getParent(OID oid) {
        return oid != null ? oid.trim() : null;
    }

    /**
     * Returns whether the MIB Uri is valid.
     *
     * @param uri the URI
     * @return {@code true} if valid,  {@code false} otherwise
     */
    public static boolean isValidMibUri(String uri) {
        try {
            return !INTERNAL_MIB_SOURCE.equals(uri) && isFileUri(URI.create(uri));
        } catch (Exception e) {
            return false;
        }
    }
}
