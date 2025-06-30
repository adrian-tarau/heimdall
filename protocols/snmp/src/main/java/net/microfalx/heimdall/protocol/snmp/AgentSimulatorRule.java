package net.microfalx.heimdall.protocol.snmp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.FormatterUtils;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.NumberUtils;
import net.microfalx.lang.ObjectUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.agent.mo.snmp.*;
import org.snmp4j.agent.request.SubRequest;
import org.snmp4j.smi.*;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static org.snmp4j.smi.SMIConstants.*;

@AllArgsConstructor
@Getter
@ToString
public class AgentSimulatorRule implements Identifiable<String> {

    private final OID oid;
    private final int type;
    private Variable value;
    boolean enabled;

    private Function function;

    @Override
    public String getId() {
        return oid.toDottedString();
    }

    /**
     * Returns whether this rule is dynamic or not.
     *
     * @return {@code true} if the rule has a function to compute the next value, {@code false} otherwise.
     */
    public boolean isDynamic() {
        return function != null;
    }

    /**
     * Returns the managed object for this rule.
     *
     * @return a non-null instance of {@link ManagedObject} if the rule is dynamic, otherwise {@code null}.
     */
    public ManagedObject<SubRequest<?>> getManagedObject() {
        MOAccessImpl moAccess = new MOAccessImpl(MOAccessImpl.ACCESSIBLE_FOR_READ_ONLY);
        if (isDynamic()) value = SnmpUtils.createVariable(getType(), convert(function.getNext(this)));
        return switch (value.getSyntax()) {
            case SYNTAX_INTEGER32 -> new EnumeratedScalar<>(oid, moAccess, (Integer32) value);
            case SYNTAX_OCTET_STRING -> new DisplayStringScalar<>(oid, moAccess, (OctetString) value);
            case SYNTAX_NULL -> new EnumeratedScalar<>(oid, moAccess, (Null) value);
            case SYNTAX_OBJECT_IDENTIFIER -> new EnumeratedScalar<>(oid, moAccess, (OID) value);
            case SYNTAX_IPADDRESS -> new EnumeratedScalar<>(oid, moAccess, (IpAddress) value);
            case SYNTAX_COUNTER32 -> new EnumeratedScalar<>(oid, moAccess, (Counter32) value);
            case SYNTAX_GAUGE32 -> new EnumeratedScalar<>(oid, moAccess, (Gauge32) value);
            case SYNTAX_TIMETICKS -> new TimeStampScalar(oid, moAccess, (TimeTicks) value, new SNMPv2MIB.SysUpTimeImpl());
            case SYNTAX_OPAQUE -> new EnumeratedScalar<>(oid, moAccess, (Opaque) value);
            case SYNTAX_COUNTER64 -> new EnumeratedScalar<>(oid, moAccess, (Counter64) value);
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

    private String convert(double value) {
        return switch (type) {
            case SYNTAX_COUNTER32, SYNTAX_INTEGER32, SYNTAX_GAUGE32, SYNTAX_TIMETICKS -> Integer.toString((int) value);
            case SYNTAX_COUNTER64 -> Long.toString((long) value);
            default -> FormatterUtils.formatNumber(value, 2);
        };
    }


    /**
     * A function to compute the next value for an OID.
     */
    public interface Function {

        /**
         * Computes the next value for the given rule.
         *
         * @param rule the rule to compute the next value for
         * @return the next value as a double
         */
        double getNext(AgentSimulatorRule rule);
    }

    /**
     * Base class for all functions.
     */
    static abstract class AbstractFunction implements Function {

        private final Number initialValue;
        private volatile Number value;

        public AbstractFunction(Number initialValue) {
            requireNonNull(initialValue);
            this.initialValue = initialValue;
            this.value = initialValue;
        }

        protected final Number getInitialValue() {
            return initialValue;
        }

        public final Number getValue() {
            return value;
        }

        protected final void setValue(Number value) {
            requireNonNull(value);
            this.value = value;
        }

    }

    public static class ConstantFunction extends AbstractFunction {

        public ConstantFunction(Number initialValue) {
            super(initialValue);
        }

        @Override
        public double getNext(AgentSimulatorRule rule) {
            return 0;
        }
    }

    public static class LinearFunction extends AbstractFunction {

        private final double increment;

        public LinearFunction(Number initialValue, double increment) {
            super(initialValue);
            this.increment = increment;
        }

        @Override
        public double getNext(AgentSimulatorRule rule) {
            double currentValue = getValue().doubleValue();
            setValue(currentValue + increment);
            return currentValue;
        }
    }

    public static class RandomFunction extends AbstractFunction {

        private final NormalDistribution normalDistribution;

        public RandomFunction(Number initialValue, float stdDev) {
            super(initialValue);
            normalDistribution = new NormalDistribution(getInitialValue().doubleValue(), stdDev);
        }

        @Override
        public double getNext(AgentSimulatorRule rule) {
            setValue(normalDistribution.sample());
            return getValue().doubleValue();
        }
    }

    public static class CumulativeFunction extends AbstractFunction {

        private int cumulative;
        private final RandomDataGenerator randomGen = new RandomDataGenerator();

        public CumulativeFunction(Number initialValue, int cumulative) {
            super(initialValue);
            this.cumulative = cumulative;
        }

        @Override
        public double getNext(AgentSimulatorRule rule) {
            for (int i = 0; i < 10; i++) {
                double value = randomGen.nextUniform(1.0, 10.0);
                cumulative += (int) value;
            }
            setValue(cumulative);
            return getValue().doubleValue();
        }
    }

}

