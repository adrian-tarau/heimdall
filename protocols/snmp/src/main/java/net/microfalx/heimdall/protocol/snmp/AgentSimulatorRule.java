package net.microfalx.heimdall.protocol.snmp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.Identifiable;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.snmp4j.smi.OID;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@AllArgsConstructor
@Getter
@ToString
public class AgentSimulatorRule implements Identifiable<String> {

    private final OID oid;
    private final int type;
    private final String value;

    private Function function;

    @Override
    public String getId() {
        return oid.toDottedString();
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

