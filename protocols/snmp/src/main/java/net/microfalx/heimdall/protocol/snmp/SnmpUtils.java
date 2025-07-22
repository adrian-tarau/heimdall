package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.metrics.Metrics;
import org.snmp4j.agent.MOGroup;
import org.snmp4j.agent.MOScope;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.agent.mo.*;
import org.snmp4j.agent.mo.snmp.RowCount;
import org.snmp4j.agent.mo.snmp.dh.UsmDHParametersImpl;
import org.snmp4j.smi.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Iterator;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Utility class for SNMP related operations.
 * This class can be extended in the future to include common SNMP operations.
 */
public class SnmpUtils {

    static Metrics METRICS = Metrics.of("SNMP");

    /**
     * Describes the scope of a Managed Object (MO) in a human-readable format.
     *
     * @param scope the scope to describe
     * @return a string representation of the scope
     */
    public static OID getScopeOid(MOScope scope) {
        requireNonNull(scope);
        if (scope instanceof MOScalar<?> scalar) {
            return scalar.getOid();
        } else {
            return scope.getLowerBound();
        }
    }

    /**
     * Describes the scope of a Managed Object (MO) in a human-readable format.
     *
     * @param scope the scope to describe
     * @return a string representation of the scope
     */
    public static String getScopeID(MOScope scope) {
        requireNonNull(scope);
        return StringUtils.toIdentifier(ObjectUtils.toString(scope.getLowerBound())
                + "_" + ObjectUtils.toString(scope.getUpperBound()));
    }

    /**
     * Describes the scope of a Managed Object (MO) in a human-readable format.
     *
     * @param scope the scope to describe
     * @return a string representation of the scope
     */
    public static String describeScope(MOScope scope) {
        StringBuilder builder = new StringBuilder();
        builder.append(getScopeSeparator(scope.isLowerIncluded(), true))
                .append(ObjectUtils.toString(scope.getLowerBound())).append("->")
                .append(ObjectUtils.toString(scope.getLowerBound()))
                .append(getScopeSeparator(scope.isUpperIncluded(), false));
        return builder.toString();
    }

    /**
     * Describes the type of a Managed Object in a human-readable format.
     *
     * @param managedObject the managed object to describe
     * @return a string representation of the managed object's type
     */
    public static String describeMoType(ManagedObject<?> managedObject) {
        if (managedObject instanceof MOScalar<?>) {
            return "Scalar";
        } else if (managedObject instanceof MOTable<?, ?, ?>) {
            return "Table";
        } else if (managedObject instanceof MOGroup) {
            return "Group";
        } else if (managedObject instanceof RowCount) {
            return "RowCount";
        } else {
            return "Unknown";
        }
    }

    /**
     * Describes the value of a Managed Object in a human-readable format.
     *
     * @param managedObject the managed object to describe
     * @return a string representation of the managed object's value
     */
    public static String describeMoValue(ManagedObject<?> managedObject, boolean includeLargeObjects) {
        if (managedObject instanceof MOScalar<?> scalar) {
            if (managedObject instanceof UsmDHParametersImpl) {
                return "<DHParameters>";
            } else {
                Variable value = scalar.getValue();
                if (value instanceof RowCount rowCount) {
                    return Long.toString(rowCount.getValue());
                } else {
                    String text = ObjectUtils.toString(value);
                    if (text.length() > 30) {
                        System.out.println("Value of " + scalar.getOid() + " is too long: " + text.length() + " characters");
                    }
                    return text;
                }
            }
        } else if (managedObject instanceof MOTable<?, ?, ?> table) {
            if (includeLargeObjects) {
                return printTable(table);
            } else {
                return "...";
            }
        } else {
            return StringUtils.NA_STRING;
        }
    }

    /**
     * Returns a string representation of a table, including its columns and rows.
     *
     * @param table the table to print
     * @return the table as a string
     */
    public static <R extends MOTableRow, C extends MOColumn<?>, M extends MOTableModel<R>> String printTable(MOTable<R, C, M> table) {
        MOTableModel<R> model = table.getModel();
        int colCount = table.getColumnCount();
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < colCount; index++) {
            C column = table.getColumn(index);
            builder.append(column.getColumnID()).append("\t");
        }
        builder.append("\n");
        colCount = model.getColumnCount();
        Iterator<R> iterator = model.iterator();
        while (iterator.hasNext()) {
            R row = iterator.next();
            for (int c = 0; c < colCount; c++) {
                Variable v = row.getValue(c);
                builder.append((v != null ? v.toString() : "null")).append("\t");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    /**
     * Describes an address in a human-readable format.
     *
     * @param address the SNMP address
     * @param <A>     the address type
     * @return a string representation of the address
     */
    public static <A extends Address> String describeAddress(A address) {
        SocketAddress socketAddress = address.getSocketAddress();
        if (socketAddress != null) {
            if (socketAddress instanceof InetSocketAddress inetSocketAddress) {
                return inetSocketAddress.getHostString() + ":" + inetSocketAddress.getPort();
            } else {
                return socketAddress.toString();
            }
        } else {
            return address.toString();
        }
    }

    /**
     * Creates a new SNMP variable based on the specified type and value.
     *
     * @param type  the type of the variable as defined in SMIConstants
     * @param value the value of the variable as a string
     * @return a non-null instance
     */
    public static Variable createVariable(int type, String value) {
        requireNonNull(value);
        return switch (type) {
            case SMIConstants.SYNTAX_INTEGER32 -> new Integer32(Integer.parseInt(value));
            case SMIConstants.SYNTAX_OCTET_STRING -> new OctetString(value);
            case SMIConstants.SYNTAX_NULL -> new Null();
            case SMIConstants.SYNTAX_OBJECT_IDENTIFIER -> new OID(value);
            case SMIConstants.SYNTAX_IPADDRESS -> new IpAddress(value);
            case SMIConstants.SYNTAX_COUNTER32 -> new Counter32(Long.parseLong(value));
            case SMIConstants.SYNTAX_GAUGE32 -> new Gauge32(Long.parseLong(value));
            case SMIConstants.SYNTAX_TIMETICKS -> new TimeTicks(Long.parseLong(value));
            case SMIConstants.SYNTAX_OPAQUE -> new Opaque(value.getBytes());
            case SMIConstants.SYNTAX_COUNTER64 -> new Counter64(Long.parseLong(value));
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

    /**
     * Returns whether the given variable is an integer type.
     *
     * @param variable the variable to check
     * @return {@code true} if the variable is an integer type, {@code false} otherwise
     */
    public static boolean isInteger(Variable variable) {
        return variable instanceof Integer32 || variable instanceof UnsignedInteger32;
    }

    private static char getScopeSeparator(boolean inclusion, boolean start) {
        if (start) {
            return inclusion ? '[' : '(';
        } else {
            return inclusion ? ']' : ')';

        }
    }
}
