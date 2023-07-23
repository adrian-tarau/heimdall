package net.microfalx.heimdall.protocol.snmp.mib;

import net.microfalx.lang.StringUtils;
import org.snmp4j.smi.OID;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilities around MIBs.
 */
public class MibUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

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
        if (oid == null) return null;
        oid.removeLast();
        return oid;
    }
}
