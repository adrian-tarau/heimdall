package net.microfalx.heimdall.protocol.snmp.mib;

import net.microfalx.lang.StringUtils;

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
}
