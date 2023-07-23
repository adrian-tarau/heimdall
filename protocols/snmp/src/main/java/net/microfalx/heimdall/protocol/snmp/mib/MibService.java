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
     * @param id the identifier
     * @return the module, null if it does not exist
     */
    public MibVariable findVariable(String id) {
        requireNonNull(id);
        return holder.variablesById.get(toIdentifier(id));
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
        Collection<MibVariable> newVariables = new ArrayList<>();
        Map<String, MibModule> newModulesById = new HashMap<>();
        Map<String, MibVariable> newVariablesById = new HashMap<>();
        for (SmiModule smiModule : mib.getModules()) {
            if (smiModule.getModuleIdentity() == null) continue;
            MibModule module = new MibModule(smiModule);
            newModules.add(module);
            newModulesById.put(module.getId(), module);
            newModulesById.put(toIdentifier(module.getOid()), module);
            for (MibVariable variable : module.getVariables()) {
                newVariables.add(variable);
                newVariablesById.putIfAbsent(variable.getId(), variable);
                newVariablesById.putIfAbsent(toIdentifier(variable.getOid()), variable);
            }
        }
        this.holder = new MibHolder(newModules, newVariables, newModulesById, newVariablesById);
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
        private Collection<MibVariable> variables = Collections.emptyList();
        private Map<String, MibModule> modulesById = Collections.emptyMap();
        private Map<String, MibVariable> variablesById = Collections.emptyMap();

        MibHolder() {
        }

        public MibHolder(Collection<MibModule> modules, Collection<MibVariable> variables, Map<String, MibModule> modulesById, Map<String, MibVariable> variablesById) {
            this.modules = modules;
            this.variables = variables;
            this.modulesById = modulesById;
            this.variablesById = variablesById;
        }
    }
}
