package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.smi.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Loads a text file containing SNMP agent simulator data and parses it into a structured format.
 */
public class AgentSimulatorParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentSimulatorParser.class);

    /**
     * Parses the given resource into a collection of agent simulator rules.
     * N
     *
     * @param resource the resource to parse
     * @return a collection of agent simulator rules
     */
    public Collection<AgentSimulatorRule> parse(Resource resource) throws IOException {
        Collection<AgentSimulatorRule> rules = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(resource.getReader())) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|");
                OID oid = new OID(data[0]);
                String typeAsString = data[1];
                int type;
                String value = "";
                if (data.length == 3) value = data[2];
                String functionType = "";
                if (typeAsString.endsWith("x") || typeAsString.endsWith("e")) {
                    type = Integer.parseInt(typeAsString.substring(0, typeAsString.length() - 1));
                } else if (typeAsString.contains(":")) {
                    functionType = typeAsString.split(":")[1];
                    type = Integer.parseInt(typeAsString.split(":")[0]);
                } else {
                    type = Integer.parseInt(typeAsString);
                }
                AgentSimulatorRule.Function function = getFunction(type, functionType, value);
                Variable variable = null;
                if (function == null) variable = SnmpUtils.createVariable(type, value);
                AgentSimulatorRule agentSimulatorRule = new AgentSimulatorRule(oid, type, variable, true, function);
                rules.add(agentSimulatorRule);
            }
        }
        return rules;
    }


    private AgentSimulatorRule.Function getFunction(int type, String functionType, String value) {
        if ("numeric".equals(functionType) && (type == 2 || type == 65 || type == 66 || type == 67 || type == 70)) {
            return getNumericFunction(value);
        } else {
            return null;
        }
    }

    private AgentSimulatorRule.Function getNumericFunction(String value) {
        String[] parameters = value.split(",");
        int parameterCount = parameters.length;
        Integer initialValue = null;
        Double rateValue = null;
        Integer cumulative = null;
        Integer deviationValue = null;
        for (String parameter : parameters) {
            String[] parts = parameter.split("=");
            if (parts.length != 2) continue;
            String paramName = parts[0].toLowerCase();
            String paramValue = parts[1];
            switch (paramName) {
                case "initial" -> initialValue = Integer.parseInt(paramValue);
                case "rate" -> rateValue = Double.parseDouble(paramValue);
                case "deviation" -> deviationValue = Integer.parseInt(paramValue);
                case "cumulative" -> cumulative = Integer.parseInt(paramValue);
            }
        }
        if (parameterCount >= 2) {
            if (initialValue != null && rateValue != null) {
                if (cumulative != null) return new AgentSimulatorRule.CumulativeFunction(initialValue, cumulative);
                return new AgentSimulatorRule.LinearFunction(initialValue, rateValue);
            } else if (initialValue != null && deviationValue != null) {
                if (cumulative != null) return new AgentSimulatorRule.CumulativeFunction(initialValue, cumulative);
                return new AgentSimulatorRule.RandomFunction(initialValue, deviationValue);
            }
        }
        LOGGER.warn("Unsupported numeric function: {}", value);
        return new AgentSimulatorRule.ConstantFunction(initialValue != null ? initialValue : 0);
    }

}
