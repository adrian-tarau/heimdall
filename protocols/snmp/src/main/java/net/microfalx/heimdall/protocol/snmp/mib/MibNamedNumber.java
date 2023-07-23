package net.microfalx.heimdall.protocol.snmp.mib;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.StringUtils;
import org.jsmiparser.smi.SmiNamedNumber;

import java.math.BigInteger;

/**
 * A MIB object which is used to associated number to names (like enum values).
 */
public class MibNamedNumber implements Identifiable<String>, Nameable {

    private final String id;
    private final SmiNamedNumber smiNamedNumber;

    MibNamedNumber(SmiNamedNumber smiNamedNumber) {
        this.smiNamedNumber = smiNamedNumber;
        this.id = StringUtils.toIdentifier(smiNamedNumber.getId());
    }

    @Override
    public String getId() {
        return smiNamedNumber.getId();
    }

    @Override
    public String getName() {
        return smiNamedNumber.getCodeId();
    }

    public BigInteger getNumber() {
        return smiNamedNumber.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MibNamedNumber that)) return false;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "MibNamedNumber{" +
                "id='" + id + '\'' +
                ", smiNamedNumber=" + smiNamedNumber +
                '}';
    }
}
