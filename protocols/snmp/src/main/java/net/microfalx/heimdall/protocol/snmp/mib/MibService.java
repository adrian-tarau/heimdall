package net.microfalx.heimdall.protocol.snmp.mib;

import net.microfalx.lang.StringUtils;
import net.microfalx.resource.ClassPathResource;
import org.jsmiparser.parser.SmiDefaultParser;
import org.jsmiparser.smi.SmiMib;
import org.jsmiparser.smi.SmiModule;
import org.jsmiparser.util.location.Location;
import org.jsmiparser.util.problem.ProblemEvent;
import org.jsmiparser.util.problem.ProblemEventHandler;
import org.jsmiparser.util.problem.annotations.ProblemSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.smi.OID;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

@Service
public class MibService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(MibService.class);

    private volatile MibHolder holder = new MibHolder();

    /**
     * Returns all registered modules.
     *
     * @return a non-null instance
     */
    public Collection<MibModule> getModules() {
        return unmodifiableCollection(holder.modules);
    }

    /**
     * Finds a module by its identifier.
     *
     * @param id the identifier
     * @return the module, null if it does not exist
     */
    public MibModule findModule(String id) {
        requireNonNull(id);
        return holder.modulesById.get(toIdentifier(id));
    }


    /**
     * Returns all registered symbols.
     *
     * @return a non-null instance
     */
    public Collection<MibSymbol> getSymbols() {
        return unmodifiableCollection(holder.symbols);
    }

    /**
     * Finds a symbol by its identifier.
     *
     * @param id the symbol identifier within a module or its OID
     * @return the module, null if it does not exist
     */
    public MibSymbol findSymbol(String id) {
        requireNonNull(id);
        return holder.symbolsById.get(toIdentifier(id));
    }

    /**
     * Returns all registered variables.
     *
     * @return a non-null instance
     */
    public Collection<MibVariable> getVariables() {
        return unmodifiableCollection(holder.variables);
    }

    /**
     * Finds a variable by its identifier.
     *
     * @param id the variable identifier within a module or its OID
     * @return the module, null if it does not exist
     */
    public MibVariable findVariable(String id) {
        requireNonNull(id);
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
        requireNonNull(value);
        if (!MibUtils.isOid(value)) throw new IllegalArgumentException("An OID is required, received '" + value + "'");
        OID oid = new OID(value);
        while (oid.size() > 0) {
            MibSymbol symbol = findSymbol(oid.toDottedString());
            if (symbol != null) {
                int length = symbol.getOid().length();
                if (length == value.length()) return symbol.getFullName();
                return symbol.getFullName() + value.substring(length);
            }
            oid = MibUtils.getParent(oid);
        }
        return describeOid(value);
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
        loadMibs();
    }

    private void loadMibs() {
        final List<URL> mibUrls = new ArrayList<>();
        try {
            ClassPathResource.directory("mib").walk((root, child) -> {
                if (child.isFile()) mibUrls.add(child.toURI().toURL());
                return true;
            });
        } catch (IOException e) {
            LOGGER.error("Failed to list internal MIBs", e);
            return;
        }
        try {
            SmiMib mib = load(mibUrls);
            extractModules(mib);
        } catch (IOException e) {
            LOGGER.error("Failed to load MIBs", e);
        }
    }

    private SmiMib load(List<URL> mibUrls) throws IOException {
        ProblemEventHandlerImpl eventHandler = new ProblemEventHandlerImpl();
        SmiDefaultParser parser = new SmiDefaultParser(eventHandler);
        parser.getFileParserPhase().setInputUrls(mibUrls);
        return parser.parse();
    }

    private void extractModules(SmiMib mib) {
        Collection<MibModule> newModules = new ArrayList<>();
        Collection<MibSymbol> newSymbols = new ArrayList<>();
        Collection<MibVariable> newVariables = new ArrayList<>();
        Map<String, MibModule> newModulesById = new HashMap<>();
        Map<String, MibSymbol> newSymbolsById = new HashMap<>();
        Map<String, MibVariable> newVariablesById = new HashMap<>();
        for (SmiModule smiModule : mib.getModules()) {
            MibModule module = new MibModule(smiModule);
            newModules.add(module);
            newModulesById.put(module.getId(), module);
            newModulesById.put(toIdentifier(module.getOid()), module);
            for (MibSymbol symbol : module.getSymbols()) {
                newSymbols.add(symbol);
                newSymbolsById.putIfAbsent(symbol.getId(), symbol);
                if (symbol.getOid() != null) newSymbolsById.putIfAbsent(toIdentifier(symbol.getOid()), symbol);
            }
            for (MibVariable variable : module.getVariables()) {
                newVariables.add(variable);
                newVariablesById.putIfAbsent(variable.getId(), variable);
                newVariablesById.putIfAbsent(toIdentifier(variable.getOid()), variable);
            }
        }
        this.holder = new MibHolder(newModules, newSymbols, newVariables, newModulesById, newSymbolsById, newVariablesById);
    }

    private static final AtomicInteger EMPTY = new AtomicInteger();

    private class ProblemEventHandlerImpl implements ProblemEventHandler {

        private final Map<ProblemSeverity, AtomicInteger> severityCounts = new HashMap<>();

        @Override
        public void handle(ProblemEvent event) {
            severityCounts.computeIfAbsent(event.getSeverity(), problemSeverity -> new AtomicInteger()).incrementAndGet();
            Location location = event.getLocation();
            if (location == null) location = new Location(StringUtils.NA_STRING, -1, -1);
            String message = "[" + location.getSource() + " - " + location.getLine() + ":" + location.getColumn() + "] " + event.getLocalizedMessage();
            switch (event.getSeverity()) {
                case FATAL, ERROR, WARNING -> LOGGER.warn(message);
                case INFO -> LOGGER.info(message);
                case DEBUG -> LOGGER.debug(message);
            }
        }

        @Override
        public boolean isOk() {
            return getSeverityCount(ProblemSeverity.ERROR) + getSeverityCount(ProblemSeverity.FATAL) == 0;
        }

        @Override
        public boolean isNotOk() {
            return false;
        }

        @Override
        public int getSeverityCount(ProblemSeverity severity) {
            return severityCounts.getOrDefault(severity, EMPTY).get();
        }

        @Override
        public int getTotalCount() {
            return severityCounts.values().stream().mapToInt(AtomicInteger::get).sum();
        }
    }

    private static class MibHolder {

        private Collection<MibModule> modules = Collections.emptyList();
        private Collection<MibSymbol> symbols = Collections.emptyList();
        private Collection<MibVariable> variables = Collections.emptyList();
        private Map<String, MibModule> modulesById = Collections.emptyMap();
        private Map<String, MibSymbol> symbolsById = Collections.emptyMap();
        private Map<String, MibVariable> variablesById = Collections.emptyMap();

        MibHolder() {
        }

        public MibHolder(Collection<MibModule> modules, Collection<MibSymbol> symbols, Collection<MibVariable> variables,
                         Map<String, MibModule> modulesById, Map<String, MibSymbol> symbolsById, Map<String, MibVariable> variablesById) {
            this.modules = modules;
            this.symbols = symbols;
            this.variables = variables;
            this.modulesById = modulesById;
            this.symbolsById = symbolsById;
            this.variablesById = variablesById;
        }
    }
}
