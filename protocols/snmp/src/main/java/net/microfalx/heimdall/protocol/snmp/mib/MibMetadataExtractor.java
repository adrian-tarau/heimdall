package net.microfalx.heimdall.protocol.snmp.mib;

import net.microfalx.bootstrap.support.report.Issue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.smi.OID;

import java.util.*;
import java.util.regex.Pattern;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A helper class which extracts information about bind variables from a MIB
 * and provides default attributes to the MIB configuration.
 */
class MibMetadataExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MibService.class);

    private static final OID ENTERPRISE_PREFIX = new OID("1.3.6.1.4.1");
    private static final int MAX_OID_COUNT = 20;

    private final MibModule module;
    private String enterpriseOid;
    private Set<String> messageOids = new LinkedHashSet<>();
    private Set<String> createdAtOids = new LinkedHashSet<>();
    private Set<String> sentAtOids = new LinkedHashSet<>();
    private Set<String> severityOids = new LinkedHashSet<>();

    public MibMetadataExtractor(MibModule module) {
        requireNonNull(module);
        this.module = module;
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
     * Returns the OID of the bind variable which holds the message associated with an SNMP event (trap).
     *
     * @return the OID, null if one cannot be provided
     */
    public Set<String> getMessageOid() {
        return messageOids.isEmpty() ? null : messageOids;
    }

    /**
     * Returns the OID of the bind variable which holds the creation time associated with an SNMP event (trap).
     *
     * @return the OID, null if one cannot be provided
     */
    public Set<String> getCreatedAtOid() {
        return createdAtOids.isEmpty() ? null : createdAtOids;
    }

    /**
     * Returns the OID of the bind variable which holds the sent time associated with an SNMP event (trap).
     *
     * @return the OID, null if one cannot be provided
     */
    public Set<String> getSentAtOid() {
        return sentAtOids.isEmpty() ? null : sentAtOids;
    }

    public Set<String> getSeverityOid() {
        return severityOids.isEmpty() ? null : severityOids;
    }

    /**
     * Invoked to process the MIB bind variables.
     */
    void execute() {
        try {
            extractEnterprise();
            extractMessage();
            extractCreated();
            extractSent();
            extractSeverity();
        } catch (Exception e) {
            Issue.create(Issue.Type.CONFIGURATION, "MIB", module.getName())
                    .withDescription(e, "Failed to detect base OIDs for ''{0}''", module.getName())
                    .withModule("SNMP").register();
        }
    }

    /**
     * Finds first bind variable which has its name matches by any of the given patterns.
     *
     * @param patterns the patters to select a variable
     * @return the variable's OID
     */
    private Set<String> findModuleOIDs(Pattern[] patterns) {
        Collection<MibVariable> variables = getVariables();
        Set<String> oids = new HashSet<>();
        for (Pattern pattern : patterns) {
            for (MibVariable variable : variables) {
                if (pattern.matcher(variable.getName()).matches()) oids.add(variable.getOid());
                if (oids.size() >= MAX_OID_COUNT) break;
            }
            if (oids.size() >= MAX_OID_COUNT) break;
        }
        return oids;
    }

    private void extractEnterprise() {
        Optional<MibVariable> firstVariable = getVariables().stream()
                .filter(v -> new OID(v.getOid()).startsWith(ENTERPRISE_PREFIX)).findFirst();
        if (firstVariable.isPresent()) {
            MibVariable variable = firstVariable.get();
            OID oid = new OID(variable.getOid());
            enterpriseOid = oid.subOID(0, 7).toDottedString();
        }
    }

    private void extractMessage() {
        messageOids = findModuleOIDs(MESSAGE_PATTERNS);
    }

    private void extractCreated() {
        createdAtOids = findModuleOIDs(CREATE_PATTERNS);
    }

    private void extractSent() {
        sentAtOids = findModuleOIDs(SEND_PATTERNS);
    }

    private void extractSeverity() {
        severityOids = findModuleOIDs(SEVERITY_PATTERNS);
    }

    private Collection<MibVariable> getVariables() {
        return MibUtils.getValid(module.getVariables());
    }

    private static final Pattern[] MESSAGE_PATTERNS = new Pattern[]{
            Pattern.compile(".*message.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*errordescription.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*perfdescription.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*healthdescription.*", Pattern.CASE_INSENSITIVE),
    };

    private static final Pattern[] CREATE_PATTERNS = new Pattern[]{
            Pattern.compile(".*createdtime.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*completiontime.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*submissiontime.*", Pattern.CASE_INSENSITIVE)
    };
    private static final Pattern[] SEND_PATTERNS = new Pattern[]{
            Pattern.compile(".*send.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*sent.*", Pattern.CASE_INSENSITIVE)
    };
    private static final Pattern[] SEVERITY_PATTERNS = new Pattern[]{
            Pattern.compile(".*severity.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*level.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*state.*", Pattern.CASE_INSENSITIVE)
    };

}
