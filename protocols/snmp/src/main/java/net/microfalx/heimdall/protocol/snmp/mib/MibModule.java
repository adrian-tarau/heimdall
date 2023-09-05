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
    @Visible(modes = {Visible.Mode.VIEW})
    String enterpriseOid;

    @Position(12)
    @Visible(modes = {Visible.Mode.VIEW})
    Set<String> messageOids;

    @Position(13)
    @Visible(modes = {Visible.Mode.VIEW})
    Set<String> createdAtOid;

    @Position(14)
    @Visible(modes = {Visible.Mode.VIEW})
    Set<String> sentAtOid;

    @Position(15)
    @Visible(modes = {Visible.Mode.VIEW})
    Set<String> severityOid;

    @Visible(false)
    private Resource content;

    MibModule(SmiModule module) {
        requireNonNull(module);
        this.module = module;
        this.id = toIdentifier(module.getId());
        this.setContent(extractResource(module));
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

    /**
     * Returns the type of the MIB.
     *
     * @return a non-null instance
     */
    public MibType getType() {
        return type;
    }

    /**
     * CHanges the type of the MIB.
     *
     * @param type the type
     */
    public void setType(MibType type) {
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
     * Returns the MIB token ID for this module (what comes before <code>DEFINITIONS ::= BEGIN</code>).
     *
     * @return a non-null instance
     */
    @Visible(false)
    public String getIdToken() {
        return module.getIdToken().getValue();
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
     * Returns all imported modules.
     *
     * @return a non-null instance
     */
    @Visible(false)
    public Collection<MibImport> getImportedModules() {
        return module.getImports().stream().map(i -> new MibImport(this, i)).toList();
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

    @Visible(modes = {Visible.Mode.VIEW})
    public String getEnterpriseOid() {
        return enterpriseOid;
    }

    @Visible(modes = {Visible.Mode.VIEW})
    public Set<String> getMessageOids() {
        return messageOids != null ? unmodifiableSet(messageOids) : emptySet();
    }

    @Visible(modes = {Visible.Mode.VIEW})
    public Set<String> getCreatedAtOids() {
        return createdAtOid != null ? unmodifiableSet(createdAtOid) : emptySet();
    }

    @Visible(modes = {Visible.Mode.VIEW})
    public Set<String> getSentAtOids() {
        return sentAtOid != null ? unmodifiableSet(sentAtOid) : emptySet();
    }

    @Visible(modes = {Visible.Mode.VIEW})
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
        return MibUtils.isValid(module) ? ResourceFactory.resolve(module.getIdToken().getLocation().getSource()) : NullResource.createNull();
    }


}
