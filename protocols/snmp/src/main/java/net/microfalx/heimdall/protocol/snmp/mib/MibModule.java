package net.microfalx.heimdall.protocol.snmp.mib;

import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.annotation.*;
import net.microfalx.resource.NullResource;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import org.jsmiparser.smi.SmiModule;
import org.jsmiparser.util.location.Location;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A MIB module.
 */
@Name("Modules")
@ReadOnly
public class MibModule implements Identifiable<String>, Nameable, Descriptable {

    @Id
    @Visible(false)
    private final String id;

    @Visible(false)
    private final SmiModule module;

    @Position(5)
    private MibType type = MibType.SYSTEM;

    @Position(10)
    private String fileName;

    @Position(11)
    @Visible(false)
    private String enterpriseOid;

    @Position(12)
    @Visible(false)
    private Set<String> messageOids;

    @Position(13)
    @Visible(false)
    private Set<String> createdAtOid;

    @Position(14)
    @Visible(false)
    private Set<String> sentAtOid;

    @Position(15)
    @Visible(false)
    private Set<String> severityOid;

    @Visible(false)
    private Resource content;

    MibModule(SmiModule module) {
        requireNonNull(module);
        this.module = module;
        this.id = toIdentifier(module.getId());
        this.setContent(extractResource(module));
        this.extractOids();
    }

    /**
     * Returns the raw module (returned by the parser).
     *
     * @return a non-null instance
     */
    public SmiModule getModule() {
        return module;
    }

    @Override

    public String getId() {
        return id;
    }

    @Position(1)
    @Override
    @Name
    public String getName() {
        return module.getId();
    }

    public MibType getType() {
        return type;
    }

    void setType(MibType type) {
        requireNonNull(type);
        this.type = type;
    }

    @Override
    @Position(100)
    @Formattable(maximumLines = 2)
    public String getDescription() {
        return module.getModuleIdentity() != null ? module.getModuleIdentity().getDescription() : null;
    }

    /**
     * Returns the OID for this module.
     *
     * @return a non-null instance
     */
    @Visible(false)
    public String getOid() {
        return module.getCodeId();
    }

    /**
     * Returns the organization.
     *
     * @return a non-null instance if the organization is defined, null otherwise
     */
    @Position(20)
    public String getOrganization() {
        return module.getModuleIdentity() != null ? module.getModuleIdentity().getOrganization() : null;
    }

    /**
     * Returns the contact information.
     *
     * @return a non-null instance if provided, null otherwise
     */
    @Visible(false)
    public String getContactInformation() {
        return module.getModuleIdentity() != null ? module.getModuleIdentity().getContactInfo() : null;
    }

    /**
     * Returns the date/time when the MIB was last modified.
     *
     * @return a non-null instance if provided, null for unknown
     */
    @Position(110)
    public ZonedDateTime getLastModified() {
        return module.getModuleIdentity() != null ? MibUtils.parseDateTime(module.getModuleIdentity().getLastUpdated()) : null;
    }

    /**
     * Returns all variables defined in the module.
     *
     * @return a non-null instance
     */
    @Visible(false)
    public Collection<MibVariable> getVariables() {
        return module.getVariables().stream().filter(MibUtils::isValid).map(v -> new MibVariable(this, v)).toList();
    }

    /**
     * Returns all symbols defined in the module.
     *
     * @return a non-null instance
     */
    @Visible(false)
    public Collection<MibSymbol> getSymbols() {
        return module.getSymbols().stream().filter(MibUtils::isValid).map(s -> new MibSymbol(this, s)).toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MibModule module = (MibModule) o;
        return Objects.equals(id, module.id);
    }

    public String getEnterpriseOid() {
        return enterpriseOid;
    }

    public Set<String> getMessageOids() {
        return messageOids != null ? unmodifiableSet(messageOids) : emptySet();
    }

    public Set<String> getCreatedAtOids() {
        return createdAtOid != null ? unmodifiableSet(createdAtOid) : emptySet();
    }

    public Set<String> getSentAtOids() {
        return sentAtOid != null ? unmodifiableSet(sentAtOid) : emptySet();
    }

    public Set<String> getSeverityOids() {
        return severityOid != null ? unmodifiableSet(severityOid) : emptySet();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Resource getContent() {
        return content;
    }

    public void setContent(Resource content) {
        requireNonNull(content);
        this.content = content;
        this.fileName = content.getFileName();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MibModule{" +
                "id=" + getId() +
                ", identify=" + module.getModuleIdentity() +
                '}';
    }

    private Resource extractResource(SmiModule module) {
        Location location = module.getIdToken().getLocation();
        if (location != null && (MibUtils.isValidMibUri(location.getSource()))) {
            return ResourceFactory.resolve(location.getSource());
        } else {
            return NullResource.createNull();
        }
    }

    private void extractOids() {
        MibMetadataExtractor extractor = new MibMetadataExtractor(this);
        extractor.execute();
        enterpriseOid = extractor.getEnterpriseOid();
        messageOids = extractor.getMessageOid();
        createdAtOid = extractor.getCreatedAtOid();
        sentAtOid = extractor.getSentAtOid();
        severityOid = extractor.getSeverityOid();
    }
}
