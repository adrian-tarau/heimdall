package net.microfalx.heimdall.protocol.snmp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.snmp4j.smi.OID;

@AllArgsConstructor
@Getter
@ToString
public class AgentSimulatorRule {

    private final OID oid;
    private final int type;
    private final String value;

    private Function function;

    /**
     * A function to compute the next value for an OID.
     */
    public interface Function {

        /**
         * Computes the next value for the given rule.
         *
         * @param rule the rule to compute the next value for
         * @return the next value as a string
         */
        String getNext(AgentSimulatorRule rule);
    }


}

