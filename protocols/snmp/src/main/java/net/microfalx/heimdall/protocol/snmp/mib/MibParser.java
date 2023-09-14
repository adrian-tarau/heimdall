package net.microfalx.heimdall.protocol.snmp.mib;

import com.google.common.collect.Iterables;
import net.microfalx.lang.EnumUtils;
import net.microfalx.resource.Resource;
import net.microfalx.resource.TemporaryFileResource;
import org.jsmiparser.parser.SmiDefaultParser;
import org.jsmiparser.phase.xref.AbstractSymbolDefiner;
import org.jsmiparser.smi.SmiMib;
import org.jsmiparser.smi.SmiModule;
import org.jsmiparser.util.location.Location;
import org.jsmiparser.util.problem.ProblemEvent;
import org.jsmiparser.util.problem.ProblemEventHandler;
import org.jsmiparser.util.problem.annotations.ProblemSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * A MIB parser.
 * <p>
 * The parser only works with URLs and we also need a way to store (cache) MIBs, so the parser
 * has a working space where all the MIBs are copied, regardless of the original resource.
 */
public class MibParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(MibParser.class);

    private final Resource resource;
    private final Set<Resource> resources = new LinkedHashSet<>();
    private final Collection<Problem> problems = new ArrayList<>();
    private final Map<String, MibType> types = new HashMap<>();
    private final Map<String, Resource> resourcesByFileName = new HashMap<>();
    private Collection<MibModule> modules = new ArrayList<>();

    public MibParser() {
        this.resource = null;
    }

    public MibParser(Resource resource) {
        requireNonNull(resource);
        this.resource = resource;
        add(resource);
    }

    public MibParser(Resource resource, MibType type) {
        requireNonNull(resource);
        this.resource = resource;
        this.add(resource, type);
    }

    /**
     * Returns the resource of the main MIB.
     *
     * @return the resource, can be NULL if there is no main MIB
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * Returns the additional resources to be parsed.
     *
     * @return a non-null instance
     */
    public Collection<Resource> getResources() {
        return unmodifiableCollection(resources);
    }

    /**
     * Returns the problems detected during parsing.
     *
     * @return a non-null instance
     */
    public Collection<Problem> getProblems() {
        return problems;
    }

    /**
     * Returns whether problems were detected during parsing.
     *
     * @return {@code true} if there are problems, {@code false} otherwise
     */
    public boolean hasProblems() {
        return !problems.isEmpty();
    }

    /**
     * Returns the problems as a report, one problem per line.
     *
     * @return a non-null instance
     */
    public String getProblemsReport() {
        return problems.stream().map(problem -> "[" + problem.getSeverity() + ": " + problem.getMessage()).collect(Collectors.joining("\n"));
    }

    /**
     * Registers an additional MIB to be parsed along the main MIB.
     * <p>
     * Additional MIBs are required to resolve symbols.
     *
     * @param resource the resource of the MIB
     */
    public MibParser add(Resource resource) {
        return add(resource, MibType.SYSTEM);
    }

    /**
     * Registers an additional MIB to be parsed along the main MIB.
     * <p>
     * Additional MIBs are required to resolve symbols.
     *
     * @param resource the resource of the MIB
     * @param type     the type of MIB
     */
    public MibParser add(Resource resource, MibType type) {
        requireNonNull(resource);
        resources.add(resource);
        String fileName = resource.getFileName().toLowerCase();
        if (!types.containsKey(fileName)) {
            types.put(fileName, type);
        }
        resourcesByFileName.put(fileName, resource);
        return this;
    }

    /**
     * Registers additional MIBs to be parsed along the main MIB.
     * <p>
     * Additional MIBs are required to resolve symbols.
     *
     * @param resources the resources
     */
    public MibParser add(Collection<Resource> resources) {
        requireNonNull(resources);
        for (Resource resource : resources) {
            add(resource);
        }
        return this;
    }

    /**
     * Returns all the modules extracted after a parsing.
     *
     * @return a non-null instance
     */
    public Collection<MibModule> getModules() {
        return unmodifiableCollection(modules);
    }

    /**
     * Parses all modules.
     *
     * @return the parsed modules
     */
    public Collection<MibModule> parseAll() {
        validate();
        Collection<SmiModule> modules = getModules(doParse(convertResourcesToURLs(resources)));
        this.modules = modules.stream().filter(m -> getResource(m) != null).map(m -> {
            Resource resource = getResource(m);
            MibModule module = new MibModule(m);
            module.setFileName(resource.getFileName());
            module.setType(types.getOrDefault(resource.getFileName().toLowerCase(), MibType.SYSTEM));
            return module;
        }).collect(Collectors.toList());
        return getModules();
    }

    /**
     * Parses all modules and returns the main module.
     *
     * @return the parsed modules
     */
    public MibModule parse() {
        validate();
        Collection<SmiModule> modules = getModules(doParse(convertResourcesToURLs(singletonList(resource))));
        if (modules.isEmpty()) {
            throw new MibException("A module could not be extracted, problems: " + getProblemsReport());
        }
        SmiModule smiModule = modules.iterator().next();
        MibModule module;
        if (resources.size() == 1) {
            module = new MibModule(smiModule);
            this.modules.add(module);
        } else {
            Collection<MibModule> mibModules = parseAll();
            module = Iterables.find(mibModules, input -> input.equals(new MibModule(smiModule)));
            if (module == null) {
                throw new MibException("A module could not be extracted, problems: " + getProblemsReport());
            }
        }
        module.setFileName(resource.getFileName());
        module.setContent(resource);
        module.setType(types.getOrDefault(resource.getFileName().toLowerCase(), MibType.SYSTEM));
        return module;
    }

    private SmiMib doParse(List<URL> urls) {
        ProblemEventHandlerImpl eventHandler = new ProblemEventHandlerImpl();
        SmiDefaultParser parser = new SmiDefaultParser(eventHandler);
        parser.getFileParserPhase().setInputUrls(urls);
        parser.getXRefPhase().addSymbolDefiner(new SymbolDefinerImpl());
        return parser.parse();
    }

    private Collection<SmiModule> getModules(SmiMib mib) {
        return mib.getModules().stream().filter(MibUtils::isValid).toList();
    }

    private List<URL> convertResourcesToURLs(Collection<Resource> resources) {
        Set<URL> urls = new HashSet<>();
        for (Resource resource : resources) {
            URL url;
            try {
                url = resource.toURL();
            } catch (Exception e) {
                Resource tmpResource = TemporaryFileResource.file(resource.getFileName() + ".tmp", null);
                tmpResource.copyFrom(resource);
                url = tmpResource.toURL();
            }
            urls.add(url);
        }
        return new ArrayList<>(urls);
    }

    private void validate() {
        problems.clear();
        modules.clear();
        if (resources.isEmpty()) throw new MibException("At least one MIB resource is required");
    }

    private Resource getResource(SmiModule module) {
        String fileName = getFileName(module);
        if (fileName == null) return null;
        Resource resource = resourcesByFileName.get(fileName.toLowerCase());
        if (resource == null) throw new MibException("A resource with file name '" + fileName + "' is not registered");
        return resource;
    }

    private String getFileName(SmiModule module) {
        String source = module.getIdToken().getLocation().getSource();
        return MibUtils.isValid(module) ? new File(source).getName() : null;
    }

    private class SymbolDefinerImpl extends AbstractSymbolDefiner {

        public SymbolDefinerImpl() {
            super("Internet");
        }

        @Override
        protected void defineSymbols() {
            LOGGER.debug("Resolve module " + getModuleId());
        }
    }

    public static class Problem {

        private final Severity severity;
        private final String message;
        private URI uri;
        private int line;
        private int column;

        Problem(Severity severity, String message) {
            requireNonNull(severity);
            requireNonNull(message);
            this.severity = severity;
            this.message = message;
        }

        public Severity getSeverity() {
            return severity;
        }

        public String getMessage() {
            return message;
        }

        public URI getUri() {
            return uri;
        }

        public int getLine() {
            return line;
        }

        public int getColumn() {
            return column;
        }

        @Override
        public String toString() {
            return "Problem{" +
                    "severity=" + severity +
                    ", message='" + message + '\'' +
                    ", line=" + line +
                    ", column=" + column +
                    ", uri=" + uri +
                    '}';
        }

        public enum Severity {
            OFF,
            DEBUG,
            INFO,
            WARNING,
            ERROR,
            FATAL
        }
    }

    private static final AtomicInteger EMPTY = new AtomicInteger();

    private class ProblemEventHandlerImpl implements ProblemEventHandler {

        private final Map<ProblemSeverity, AtomicInteger> severityCounts = new HashMap<>();

        @Override
        public void handle(ProblemEvent event) {
            URI uri = null;
            int line = -1;
            int column = -1;
            Location location = event.getLocation();
            if (location != null) {
                if (isNotEmpty(location.getSource())) {
                    try {
                        uri = URI.create(location.getSource());
                    } catch (Exception e) {
                        // if we cannot parse there is no location
                    }
                }
                line = location.getLine();
                column = location.getColumn();
            }
            severityCounts.computeIfAbsent(event.getSeverity(), problemSeverity -> new AtomicInteger()).incrementAndGet();
            Problem problem = new Problem(EnumUtils.fromName(Problem.Severity.class, event.getSeverity().name(), Problem.Severity.ERROR), event.getLocalizedMessage());
            problem.uri = uri;
            problem.line = line;
            problem.column = column;
            problems.add(problem);
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
}
