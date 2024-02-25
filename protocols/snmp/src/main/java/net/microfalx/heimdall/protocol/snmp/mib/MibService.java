package net.microfalx.heimdall.protocol.snmp.mib;

import net.microfalx.bootstrap.core.async.TaskExecutorFactory;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.search.Document;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.heimdall.protocol.snmp.jpa.SnmpMib;
import net.microfalx.heimdall.protocol.snmp.jpa.SnmpMibRepository;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.smi.OID;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static java.lang.String.join;
import static java.time.Duration.ofSeconds;
import static java.util.Collections.unmodifiableList;
import static net.microfalx.heimdall.protocol.core.ProtocolConstants.MAX_DESCRIPTION_LENGTH;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.ConcurrencyUtils.await;
import static net.microfalx.lang.FormatterUtils.formatNumber;
import static net.microfalx.lang.StringUtils.*;
import static org.apache.commons.lang3.StringUtils.abbreviate;

@Service
public class MibService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(MibService.class);

    private volatile MibHolder holder = new MibHolder();
    private final Set<String> registeredModules = new CopyOnWriteArraySet<>();
    private final Set<String> importedModules = new CopyOnWriteArraySet<>();
    private final Set<String> resolvedModules = new CopyOnWriteArraySet<>();

    @Autowired
    private SnmpMibRepository snmpMibRepository;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private IndexService indexService;

    @Autowired(required = false)
    private AsyncTaskExecutor taskExecutor;

    private final CountDownLatch latch = new CountDownLatch(1);
    private static final Duration DEFAULT_WAIT = ofSeconds(60);

    private Resource workspace;

    /**
     * Returns the task scheduler associated with this service.
     *
     * @return a non-null instance
     */
    public AsyncTaskExecutor getTaskExecutor() {
        if (taskExecutor == null) taskExecutor = TaskExecutorFactory.create("mib").createExecutor();
        return taskExecutor;
    }

    /**
     * Returns all registered modules.
     *
     * @return a non-null instance
     */
    public List<MibModule> getModules() {
        waitInitialization();
        return unmodifiableList(holder.modules);
    }

    /**
     * Loads MIBs from a file resource or directory with resources into the user space.
     *
     * @param resource the resource or resources
     */
    public void loadModules(Resource resource) {
        requireNonNull(resource);
        waitInitialization();
        doRegisterMibs(resource, MibType.USER);
        loadModulesFromDatabaseAndResolve();
    }

    /**
     * Loads and parses a new MIB to extract its metadata.
     *
     * @param resource the resource containing the content of the MIB.
     * @return the MIB information
     */
    public MibModule parseModule(Resource resource) {
        requireNonNull(resource);
        MibParser parser = new MibParser(resource, MibType.USER);
        return parser.parse();
    }

    /**
     * Registers a new MIB or updates (based on module identifier) an existing MIB
     *
     * @param mibModule the MIB (module)
     */
    public void updateModule(MibModule mibModule) {
        requireNotEmpty(mibModule);
        persistMib(mibModule);
        taskExecutor.submit(this::loadModulesFromDatabaseAndResolve);
    }

    /**
     * Finds a module by its identifier.
     *
     * @param id the identifier
     * @return the module, null if it does not exist
     */
    public MibModule findModule(String id) {
        requireNonNull(id);
        waitInitialization();
        return holder.modulesById.get(toIdentifier(id));
    }


    /**
     * Returns all registered symbols.
     *
     * @return a non-null instance
     */
    public List<MibSymbol> getSymbols() {
        waitInitialization();
        return unmodifiableList(holder.symbols);
    }

    /**
     * Finds a symbol by its identifier.
     *
     * @param id the symbol identifier within a module or its OID
     * @return the module, null if it does not exist
     */
    public MibSymbol findSymbol(String id) {
        requireNonNull(id);
        waitInitialization();
        return holder.symbolsById.get(toIdentifier(id));
    }

    /**
     * Returns all registered variables.
     *
     * @return a non-null instance
     */
    public List<MibVariable> getVariables() {
        waitInitialization();
        return unmodifiableList(holder.variables);
    }

    /**
     * Finds a variable by its identifier.
     *
     * @param id the variable identifier within a module or its OID
     * @return the module, null if it does not exist
     */
    public MibVariable findVariable(String id) {
        requireNonNull(id);
        waitInitialization();
        return holder.variablesById.get(toIdentifier(id));
    }

    /**
     * Returns the closest name for given OID.
     * <p>
     * The method tries to find first a module, variable, or any definition by an exact match.
     * <p>
     * If there is no 1:1 mapping, it finds the closest match and appends the OID suffix.
     *
     * @param value the object identifier
     * @return the name or the object identifier if there is no match
     */
    public String findName(String value) {
        return findName(value, true, true);
    }

    /**
     * Returns the closest name for given OID.
     * <p>
     * The method tries to find first a module, variable, or any definition by an exact match.
     * <p>
     * If there is no 1:1 mapping, it finds the closest match and appends the OID suffix.
     *
     * @param value    the object identifier
     * @param fullName {@code true} to return full name (module + name), {@code false} otherwie
     * @return the name or the object identifier if there is no match
     */
    public String findName(String value, boolean fullName, boolean suffix) {
        requireNonNull(value);
        if (!MibUtils.isOid(value)) throw new IllegalArgumentException("An OID is required, received '" + value + "'");
        OID oid = new OID(value);
        while (oid.size() > 0) {
            MibSymbol symbol = findSymbol(oid.toDottedString());
            if (symbol != null) {
                int length = symbol.getOid().length();
                String name = fullName ? symbol.getFullName() : symbol.getName();
                if (length == value.length()) return name;
                return suffix ? name + value.substring(length) : name;
            }
            oid = MibUtils.getParent(oid);
        }
        return value;
    }

    /**
     * Describes the OID based on symbols defined in registered MIBs.
     *
     * @param value the OID
     * @return the description
     */
    public String describeOid(String value) {
        requireNonNull(value);
        if (!MibUtils.isOid(value)) throw new IllegalArgumentException("An OID is required, received '" + value + "'");
        OID oid = new OID(value);
        Deque<String> names = new ArrayDeque<>();
        while (oid.size() > 0) {
            MibSymbol symbol = findSymbol(oid.toDottedString());
            if (symbol != null) {
                names.add(symbol.getName());
            } else {
                names.push(Integer.toString(oid.last()));
            }
            oid = MibUtils.getParent(oid);
        }
        StringBuilder builder = new StringBuilder();
        while (!names.isEmpty()) {
            if (builder.length() > 0) builder.append(".");
            builder.append(names.pollLast());
        }
        return builder.toString();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initWorkspace();
        getTaskExecutor().execute(this::initializeMibs);
    }

    private void registerSystemMibs() {
        Resource mibs = ClassPathResource.directory("mib");
        doRegisterMibs(mibs, MibType.SYSTEM);
    }

    private boolean isRegisteredOrResolved(String tokenId) {
        String tokenIdLc = tokenId.toLowerCase();
        boolean registered = registeredModules.contains(tokenIdLc) || resolvedModules.contains(tokenIdLc);
        if (!registered && tokenIdLc.endsWith("mib")) {
            tokenId = tokenIdLc.substring(0, tokenId.length() - 3);
        }
        return registeredModules.contains(tokenIdLc) || resolvedModules.contains(tokenIdLc);
    }

    private boolean resolveMissingMibs() {
        int missingCount = 0;
        for (String importedModule : importedModules) {
            if (!isRegisteredOrResolved(importedModule)) missingCount++;
        }
        if (missingCount == 0) return false;
        int fetchedCount = 0;
        Collection<Resource> externalResources = new ArrayList<>();
        for (String importedModule : importedModules) {
            if (isRegisteredOrResolved(importedModule)) continue;
            MibFetcher fetcher = MibFetcher.create(importedModule);
            try {
                Resource resource = fetcher.execute();
                if (resource.exists()) externalResources.add(resource);
                fetchedCount++;
            } catch (Exception e) {
                LOGGER.error("Failed to fetch module '{}' from external registries", importedModule, e);
            }
        }
        doRegisterMibs(externalResources, MibType.IMPORT);
        return fetchedCount > 0;
    }

    private void doRegisterMibs(Resource resource, MibType type) {
        final Collection<Resource> resources = discoverMibs(resource, type);
        if (!resources.isEmpty()) doRegisterMibs(resources, type);
    }

    private void doRegisterMibs(Collection<Resource> resources, MibType type) {
        LOGGER.info("Register " + formatNumber(resources.size()) + " MIBs, type " + type);
        for (Resource resource : resources) {
            LOGGER.debug(" - " + resource.toURI());
            MibParser parser = new MibParser(resource, type);
            try {
                MibModule module = parser.parse();
                extractImports(module);
                persistMib(module);
                registeredModules.add(module.getIdToken().toLowerCase());
            } catch (Exception e) {
                LOGGER.error("Failed to register MIB from " + resource.toURI(), e);
            }
        }
    }

    private SnmpMib persistMib(MibModule module) {
        try {
            SnmpMib snmpMib = snmpMibRepository.findByModuleId(module.getId());
            if (snmpMib == null) {
                snmpMib = new SnmpMib();
                snmpMib.setType(module.getType());
                snmpMib.setModuleId(module.getId());
                snmpMib.setCreatedAt(LocalDateTime.now());
            }
            snmpMib.setName(module.getName());
            snmpMib.setFileName(module.getFileName());
            try {
                snmpMib.setContent(module.getContent().loadAsString());
            } catch (Exception e) {
                throw new MibException("Fail to read the content of the MIB module " + module.getName(), e);
            }
            snmpMib.setModifiedAt(LocalDateTime.now());
            snmpMib.setDescription(abbreviate(removeLineBreaks(module.getDescription()), MAX_DESCRIPTION_LENGTH));
            snmpMibRepository.saveAndFlush(snmpMib);
            return snmpMib;
        } catch (MibException e) {
            LOGGER.error("Failed to persist MIB '{}'", module.getName(), e);
            return null;
        }
    }

    private void indexMib(MibModule module, SnmpMib snmpMib) {
        try {
            Document document = Document.create("mib_" + module.getId(), module.getName());
            document.setOwner("snmp");
            document.setType("mib");
            document.setDescription(module.getDescription());
            document.setBody(module.getContent());
            document.setReference("/admin/protocol/snmp/module#/view/" + module.getId());
            if (snmpMib != null) {
                document.setCreatedAt(snmpMib.getCreatedAt());
                document.setModifiedAt(snmpMib.getModifiedAt());
            } else {
                document.setCreatedAt(module.getLastModified());
                document.setModifiedAt(module.getLastModified());
            }
            document.add("mib_type", module.getType().name().toLowerCase());
            document.add("organization", module.getOrganization());
            document.add("enterprise_oid", module.getEnterpriseOid());
            document.add("imports", module.getImportedModules().stream().map(MibImport::getName)
                    .collect(Collectors.joining(" ")));
            indexService.index(document, false);
        } catch (Exception e) {
            LOGGER.error("Failed to index module '{}'", module.getName(), e);
        }
    }

    private void indexSymbol(MibSymbol symbol) {
        try {
            Document document = Document.create("symbol_" + symbol.getId(), symbol.getName());
            document.setOwner("snmp");
            document.setType("symbol");
            if (symbol.getDescription() != null) {
                document.setDescription(symbol.getDescription());
                document.setBody(MemoryResource.create(symbol.getDescription()));
            }
            document.setReference("/admin/protocol/snmp/symbol#/view/" + symbol.getId());
            document.add("oid", symbol.getOid());
            document.add("symbol_type", symbol.getType().name());
            document.add("module", symbol.getModule().getName());
            if (symbol.getPrimitiveType() != null) {
                document.add("primitive_type", symbol.getPrimitiveType().name());
            }
            indexService.index(document, false);
        } catch (Exception e) {
            LOGGER.error("Failed to index symbol '{}'", symbol.getName(), e);
        }
    }

    private void indexVariable(MibVariable variable) {
        try {
            Document document = Document.create("variable_" + variable.getId(), variable.getName());
            document.setOwner("snmp");
            document.setType("variable");
            if (variable.getDescription() != null) {
                document.setDescription(variable.getDescription());
                document.setBody(MemoryResource.create(variable.getDescription()));
            }
            document.setReference("/admin/protocol/snmp/variable#/view/" + variable.getId());
            document.add("oid", variable.getOid());
            document.add("module", variable.getModule().getName());
            if (variable.getUnits() != null) document.add("units", variable.getUnits());
            if (variable.getType() != null) document.add("variable_type", variable.getType().name());
            document.add("is_enum", variable.isEnum());
            if (variable.isEnum()) document.add("enum_values", variable.getEnumValues().stream()
                    .map(MibNamedNumber::getName).collect(Collectors.joining(" ")));
            document.add("is_number", variable.isNumber());
            indexService.index(document, false);
        } catch (Exception e) {
            LOGGER.error("Failed to index variable '{}'", variable.getName(), e);
        }
    }

    private void loadModulesFromDatabaseAndResolve() {
        int maxRetries = 5;
        while (maxRetries-- > 0) {
            loadModulesFromDatabase();
            if (!resolveMissingMibs()) break;
        }
        updateModuleOidsInDatabase();
    }

    private void loadModulesFromDatabase() {
        List<MibModule> newModules = new ArrayList<>();
        List<MibSymbol> newSymbols = new ArrayList<>();
        List<MibVariable> newVariables = new ArrayList<>();
        Map<String, MibModule> newModulesById = new HashMap<>();
        Map<String, MibSymbol> newSymbolsById = new HashMap<>();
        Map<String, MibVariable> newVariablesById = new HashMap<>();
        MibParser parser = new MibParser();
        for (SnmpMib snmpMib : snmpMibRepository.findAll()) {
            Resource resource = MemoryResource.create(snmpMib.getContent(), snmpMib.getFileName());
            resource = copyWorkspace(resource);
            parser.add(resource, snmpMib.getType());
        }
        Collection<MibModule> modules = parser.parseAll();
        LOGGER.info("Load modules from database");
        for (MibModule module : modules) {
            LOGGER.debug(" - " + module.getName() + ", symbols " + module.getSymbols().size() + ", variables " + module.getVariables().size());
            newModules.add(module);
            extractOids(module);
            newModulesById.put(module.getId(), module);
            newModulesById.put(toIdentifier(module.getIdToken()), module);
            for (MibSymbol symbol : module.getSymbols()) {
                newSymbols.add(symbol);
                newSymbolsById.putIfAbsent(symbol.getId(), symbol);
                if (symbol.getOid() != null) newSymbolsById.putIfAbsent(toIdentifier(symbol.getOid()), symbol);
            }
            for (MibVariable variable : module.getVariables()) {
                newVariables.add(variable);
                newVariablesById.putIfAbsent(variable.getId(), variable);
                if (variable.getOid() != null) newVariablesById.putIfAbsent(toIdentifier(variable.getOid()), variable);
            }
            if (module.getType() == MibType.IMPORT) {
                this.resolvedModules.add(module.getIdToken().toLowerCase());
            }
        }
        this.holder = new MibHolder(newModules, newSymbols, newVariables, newModulesById, newSymbolsById, newVariablesById);
        getTaskExecutor().submit(this::index);
    }

    private void index() {
        for (MibModule module : holder.modules) {
            indexMib(module, null);
        }
        for (MibSymbol symbol : holder.symbols) {
            indexSymbol(symbol);
        }
        for (MibVariable variable : holder.variables) {
            indexVariable(variable);
        }
        indexService.flush();
    }

    private void updateModuleOidsInDatabase() {
        LOGGER.info("Update modules OIDs into database");
        Metadata<SnmpMib, Field<SnmpMib>, Long> metadata = metadataService.getMetadata(SnmpMib.class);
        for (MibModule module : holder.modules) {
            SnmpMib snmpMib = snmpMibRepository.findByModuleId(module.getId());
            if (snmpMib == null) continue;
            SnmpMib snmpMibCopy = metadata.copy(snmpMib);
            snmpMibCopy.setEnterpriseOid(module.getEnterpriseOid());
            snmpMibCopy.setMessageOids(defaultIfEmpty(snmpMibCopy.getMessageOids(), nullIfEmpty(join(",", module.getMessageOids()))));
            snmpMibCopy.setSeverityOids(defaultIfEmpty(snmpMibCopy.getSeverityOids(), nullIfEmpty(join(",", module.getSeverityOids()))));
            snmpMibCopy.setCreateAtOids(defaultIfEmpty(snmpMibCopy.getCreateAtOids(), nullIfEmpty(join(",", module.getCreatedAtOids()))));
            snmpMibCopy.setSentAtOids(defaultIfEmpty(snmpMibCopy.getSentAtOids(), nullIfEmpty(join(",", module.getSentAtOids()))));
            if (!metadata.identical(snmpMib, snmpMibCopy)) {
                try {
                    snmpMibRepository.saveAndFlush(snmpMibCopy);
                } catch (Exception e) {
                    LOGGER.error("Failed to update MIB OIDs for " + module.getName(), e);
                }
            }
        }
    }

    private Collection<Resource> discoverMibs(Resource resource, MibType type) {
        final List<Resource> resources = new ArrayList<>();
        try {
            if (resource.isFile()) {
                resources.add(resource);
            } else {
                resource.walk((root, child) -> {
                    if (child.isFile()) resources.add(child);
                    return true;
                });
            }
            LOGGER.info("Discovered " + formatNumber(resources.size()) + " MIBs from " + resource.toURI() + ", type " + type);
        } catch (IOException e) {
            LOGGER.error("Failed to discover MIBs from " + resource.toURI(), e);
        }
        return resources;
    }

    private void initWorkspace() {
        workspace = resourceService.getPersisted("mib");
        LOGGER.info("MIB workspace is " + workspace.toURI());
    }

    private void waitInitialization() {
        await(latch, DEFAULT_WAIT, l -> LOGGER.warn("Timeout waiting for MIB initialization"));
    }

    private void initializeMibs() {
        try {
            registerSystemMibs();
            loadModulesFromDatabaseAndResolve();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize system MIBs");
        } finally {
            latch.countDown();
        }
    }

    private void extractImports(MibModule module) {
        for (MibImport importedModule : module.getImportedModules()) {
            this.importedModules.add(importedModule.getIdToken());
        }
    }

    private void extractOids(MibModule module) {
        MibMetadataExtractor extractor = new MibMetadataExtractor(module);
        extractor.execute();
        module.enterpriseOid = extractor.getEnterpriseOid();
        module.messageOids = extractor.getMessageOid();
        module.createdAtOid = extractor.getCreatedAtOid();
        module.sentAtOid = extractor.getSentAtOid();
        module.severityOid = extractor.getSeverityOid();
    }

    private Resource copyWorkspace(Resource resource) {
        Resource workspaceResource = workspace.resolve(String.valueOf(resource.getFileName().charAt(0)).toLowerCase(), Resource.Type.DIRECTORY);
        workspaceResource = workspaceResource.resolve(resource.getFileName(), Resource.Type.FILE);
        return workspaceResource.copyFrom(resource);
    }

    private static class MibHolder {

        private final List<MibModule> modules = new ArrayList<>();
        private final List<MibSymbol> symbols = new ArrayList<>();
        private final List<MibVariable> variables = new ArrayList<>();
        private final Map<String, MibModule> modulesById = new HashMap<>();
        private final Map<String, MibSymbol> symbolsById = new HashMap<>();
        private final Map<String, MibVariable> variablesById = new HashMap<>();

        MibHolder() {
        }

        public MibHolder(List<MibModule> modules, List<MibSymbol> symbols, List<MibVariable> variables,
                         Map<String, MibModule> modulesById, Map<String, MibSymbol> symbolsById, Map<String, MibVariable> variablesById) {
            this.modules.addAll(modules);
            this.symbols.addAll(symbols);
            this.variables.addAll(variables);
            this.modulesById.putAll(modulesById);
            this.symbolsById.putAll(symbolsById);
            this.variablesById.putAll(variablesById);
        }
    }
}
