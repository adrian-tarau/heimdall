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
        return Collections.unmodifiableCollection(holder.modules.values());
    }

    /**
     * Finds a module by its identifier.
     *
     * @param id the identifier
     * @return the module, null if it does not exist
     */
    public MibModule findModule(String id) {
        requireNonNull(id);
        return holder.modules.get(toIdentifier(id));
    }

    /**
     * Returns all registered variables.
     *
     * @return a non-null instance
     */
    public Collection<MibVariable> getVariables() {
        return Collections.unmodifiableCollection(holder.variables.values());
    }

    /**
     * Finds a variable by its identifier.
     *
     * @param id the identifier
     * @return the module, null if it does not exist
     */
    public MibVariable findVariable(String id) {
        requireNonNull(id);
        return holder.variables.get(toIdentifier(id));
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
        Map<String, MibModule> newModules = new HashMap<>();
        Map<String, MibVariable> newVariables = new HashMap<>();
        for (SmiModule smiModule : mib.getModules()) {
            MibModule module = new MibModule(smiModule);
            newModules.put(module.getId(), module);
            newModules.put(module.getOid(), module);
            for (MibVariable variable : module.getVariables()) {
                newVariables.putIfAbsent(variable.getId(), variable);
                newVariables.putIfAbsent(variable.getOid(), variable);
            }
        }
        this.holder = new MibHolder(newModules, newVariables);
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

        private Map<String, MibModule> modules = Collections.emptyMap();
        private Map<String, MibVariable> variables = Collections.emptyMap();

        MibHolder() {
        }

        MibHolder(Map<String, MibModule> modules, Map<String, MibVariable> variables) {
            this.modules = modules;
            this.variables = variables;
        }
    }
}
