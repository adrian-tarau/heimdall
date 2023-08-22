package net.microfalx.heimdall.protocol.snmp.mib;

import org.snmp4j.smi.OID;

import java.util.Optional;
import java.util.regex.Pattern;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A helper class which extracts information about bind variables from a MIB
 * and provides default attributes to the MIB configuration.
 */
class MibMetadataExtractor {

    private static final OID ENTERPRISE_PREFIX = new OID("1.3.6.1.4.1");

    private final MibModule module;
    private String messageOid;
    private String enterpriseOid;
    private String createdAtOid;
    private String sentAtOid;
    private String severityOid;

    public MibMetadataExtractor(MibModule module) {
        requireNonNull(module);
        this.module = module;
    }

    /**
     * Invoked to process the MIB bind variables.
     */
    void execute() {
        extractEnterprise();
        extractMessage();
        extractCreated();
        extractSent();
        extractSeverity();
    }

    /**
     * Finds first bind variable which has its name matches by any of the given patterns.
     *
     * @param patterns the patters to select a variable
     * @return the variable's OID, null if there is no match
     */
    private String findModuleOID(Pattern[] patterns) {
        Optional<MibVariable> first = module.getVariables().stream().filter(v -> matches(v.getName(), patterns)).findFirst();
        return first.isPresent() ? first.get().getOid() : null;
    }

    /**
     * Returns the OID of the bind variable which holds the message associated with an SNMP event (trap).
     *
     * @return the OID, null if one cannot be provided
     */
    public String getMessageOid() {
        return messageOid;
    }

    /**
     * Returns the OID of the bind variable which holds the enterprise OID associated with an SNMP event (trap).
     *
     * @return the OID, null if one cannot be provided
     */
    public String getEnterpriseOid() {
        return enterpriseOid;
    }

    /**
     * Returns the OID of the bind variable which holds the creation time associated with an SNMP event (trap).
     *
     * @return the OID, null if one cannot be provided
     */
    public String getCreatedAtOid() {
        return createdAtOid;
    }

    /**
     * Returns the OID of the bind variable which holds the sent time associated with an SNMP event (trap).
     *
     * @return the OID, null if one cannot be provided
     */
    public String getSentAtOid() {
        return sentAtOid;
    }

    public String getSeverityOid() {
        return severityOid;
    }

    private void extractEnterprise() {
        Optional<MibVariable> firstVariable = module.getVariables().stream()
                .filter(v -> ENTERPRISE_PREFIX.startsWith(new OID(v.getOid()))).findFirst();
        if (firstVariable.isPresent()) {
            MibVariable variable = firstVariable.get();
            OID oid = new OID(variable.getOid());
            enterpriseOid = oid.subOID(0, 7).toDottedString();
        }
    }

    private boolean matches(String name, Pattern[] patterns) {
        for (Pattern pattern : patterns) {
            return pattern.matcher(name).matches();
        }
        return false;
    }

    private void extractMessage() {
        messageOid = findModuleOID(MESSAGE_PATTERNS);
    }

    private void extractCreated() {
        createdAtOid = findModuleOID(Create_Patterns);
    }

    private void extractSent() {
        sentAtOid = findModuleOID(Send_Patterns);
    }

    private void extractSeverity() {
        severityOid = findModuleOID(Severity_Patterns);
    }

    private Pattern[] MESSAGE_PATTERNS = new Pattern[]{
            Pattern.compile(".*message.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*description.*", Pattern.CASE_INSENSITIVE),
    };

    private Pattern[] Create_Patterns = new Pattern[]{
            Pattern.compile(".*create.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*created.*", Pattern.CASE_INSENSITIVE)
    };
    private Pattern[] Send_Patterns = new Pattern[]{
            Pattern.compile(".*send.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*sent.*", Pattern.CASE_INSENSITIVE)
    };
    private Pattern[] Severity_Patterns = new Pattern[]{
            Pattern.compile(".*severity.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*level.*", Pattern.CASE_INSENSITIVE)
    };

}
