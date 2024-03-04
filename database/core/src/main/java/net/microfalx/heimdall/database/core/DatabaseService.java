package net.microfalx.heimdall.database.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.microfalx.bootstrap.jdbc.support.Snapshot;
import net.microfalx.bootstrap.jdbc.support.*;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import net.microfalx.resource.rocksdb.RocksDbResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import static net.microfalx.bootstrap.jdbc.support.DatabaseUtils.AVAILABILITY_INTERVAL;
import static net.microfalx.bootstrap.jdbc.support.DatabaseUtils.CONNECT_TIMEOUT;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.toIdentifier;

@Service("heimdallDatabaseService")
public class DatabaseService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseService.class);

    @Autowired
    private net.microfalx.bootstrap.jdbc.support.DatabaseService databaseService;

    @Autowired
    private DatabaseProperties databaseProperties;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private SchemaRepository schemaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SnapshotRepository snapshotRepository;

    @Autowired
    private StatementRepository statementRepository;

    @Autowired
    private TaskScheduler taskScheduler;

    private final Map<String, Schema> schemaCache = new ConcurrentHashMap<>();
    private final Map<Integer, String> schemaIdCache = new ConcurrentHashMap<>();
    private final Map<String, User> userCache = new ConcurrentHashMap<>();
    private final Set<String> persistedStatements = new ConcurrentSkipListSet<>();
    private Resource snapshotsResource;
    private Resource statementsResource;

    private static final DateTimeFormatter DIRECTORY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String FILE_NAME_FORMAT = "%09d";
    private static final LocalDateTime STARTUP = LocalDateTime.now();
    private static final Map<LocalDate, AtomicInteger> resourceSequences = new ConcurrentHashMap<>();

    net.microfalx.bootstrap.jdbc.support.DatabaseService getDatabaseService() {
        return databaseService;
    }

    public Resource getSnapshotsResource() {
        return snapshotsResource;
    }

    SchemaRepository getDatabaseSchemaRepository() {
        return schemaRepository;
    }

    SnapshotRepository getDatabaseSnapshotRepository() {
        return snapshotRepository;
    }

    /**
     * Finds a database instance from a registered schema.
     *
     * @param schema the schema
     * @return the database, null if it does not exist
     */
    Database findDatabase(Schema schema) {
        requireNonNull(schema);
        String databaseId = schemaIdCache.get(schema.getId());
        if (databaseId != null) {
            try {
                return databaseService.getDatabase(databaseId);
            } catch (DatabaseNotFoundException e) {
                // in such cases, just report "I do not know"
            }
        }
        return null;
    }

    /**
     * Finds a schema by its database metadata (and cache it for future uses).
     *
     * @param database the database metadata
     * @return a non-null instance if exists, null otherwise
     */
    Schema findSchema(Database database) {
        requireNotEmpty(database);
        Schema locatedSchema = schemaCache.get(database.getId());
        if (locatedSchema == null) {
            String uri = database.getDataSource().getUri().toASCIIString();
            for (Schema schema : schemaRepository.findAll()) {
                if (schema.getUrl().equalsIgnoreCase(uri)) {
                    locatedSchema = schema;
                    break;
                }
            }
        }
        return locatedSchema;
    }

    /**
     * Finds a user by its user-name (and cache it for future uses).
     * <p>
     * If the user does not exist, one is created.
     *
     * @param userName the username
     * @return a non-null instance
     */
    User findUser(Schema schema, String userName) {
        requireNotEmpty(userName);
        User user = userCache.get(userName.toLowerCase());
        if (user == null) {
            Optional<User> result = userRepository.findByName(userName);
            if (result.isEmpty()) {
                user = new User();
                user.setName(userName);
                user.setCreatedAt(LocalDateTime.now());
                try {
                    userRepository.saveAndFlush(user);
                } catch (DataIntegrityViolationException e) {
                    result = userRepository.findByName(userName);
                }
            }
            if (result.isEmpty()) {
                throw new DatabaseException("A database user could not be located for '" + userName + "'");
            }
            user = result.get();
            userCache.put(userName.toLowerCase(), user);
        }
        return user;
    }

    /**
     * Persists a snapshot in database and resource.
     *
     * @param statement the statement
     */
    void persistStatement(Schema schema, Session session, net.microfalx.bootstrap.jdbc.support.Statement statement) {
        requireNonNull(statement);
        if (persistedStatements.contains(statement.getId())) return;
        User user = findUser(schema, session.getUserName());
        Optional<Statement> result = statementRepository.findByStatementId(Statement.getStatementId(statement, user.getName()));
        if (result.isEmpty()) {
            try {
                Resource resource = writeStatement(statement);
                Statement databaseStatement = Statement.from(schema, user, statement, resource);
                statementRepository.saveAndFlush(databaseStatement);
                persistedStatements.add(statement.getId());
            } catch (DataIntegrityViolationException e) {
                // already there, just ignore
            } catch (Exception e) {
                throw new DatabaseException("Statement '" + org.apache.commons.lang3.StringUtils.abbreviate(statement.getContent(),
                        50) + "' cannot be stored in resource", e);
            }
        }
    }

    /**
     * Stores a snapshot serialized in an external resource.
     *
     * @param snapshot the snapshot
     * @return the resource where the snapshot was stored
     * @throws IOException I/O exception if snapshot cannot be stored
     */
    Resource writeSnapshot(Snapshot snapshot) throws IOException {
        Resource directory = snapshotsResource.resolve(DIRECTORY_DATE_FORMATTER.format(LocalDateTime.now()), Resource.Type.DIRECTORY);
        Resource target = directory.resolve(String.format(FILE_NAME_FORMAT, getNextSequence()));
        if (!directory.exists()) directory.create();
        snapshot.serialize(target.getOutputStream());
        return target;
    }

    /**
     * Stores a statement in an external resource.
     *
     * @param statement the statement
     * @return the resource where the statement was stored
     * @throws IOException I/O exception if statement cannot be stored
     */
    Resource writeStatement(net.microfalx.bootstrap.jdbc.support.Statement statement) throws IOException {
        Resource directory = statementsResource.resolve(DIRECTORY_DATE_FORMATTER.format(LocalDateTime.now()), Resource.Type.DIRECTORY);
        Resource target = directory.resolve(String.format(FILE_NAME_FORMAT, getNextSequence()));
        if (!directory.exists()) directory.create();
        target.copyFrom(MemoryResource.create(statement.getContent()));
        return target;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        reload();
        scheduleTasks();
        initResources();
    }

    public void reload() {
        LOGGER.info("Register databases (schemas)");
        for (Schema schema : schemaRepository.findAll()) {
            String id = toIdentifier(schema.getName() + "_" + schema.getType());
            schemaIdCache.put(schema.getId(), id);
            try {
                DataSource dataSource = create(id, schema.getName(), schema.getUrl(), schema.getUsername(),
                        schema.getPassword());
                dataSource = dataSource.withProperties(loadProperties(schema))
                        .withZoneId(ZoneId.of(schema.getTimeZone()))
                        .withDescription(schema.getDescription());
                databaseService.registerDataSource(dataSource);
            } catch (Exception e) {
                LOGGER.error("Failed to register data source for database " + schema.getName(), e);
            }
        }
    }

    private void scheduleTasks() {
        taskScheduler.schedule(new SnapshotsTask(this), new PeriodicTrigger(databaseProperties.getInterval()));
    }

    private Resource getSharedResource() {
        return resourceService.getShared("db");
    }

    private void initResources() {
        snapshotsResource = getSharedResource().resolve("snapshots", Resource.Type.DIRECTORY);
        if (snapshotsResource.isLocal()) {
            LOGGER.info("Database snapshots are stored in a RocksDB database: " + snapshotsResource);
            Resource dbSnapshotsResource = RocksDbResource.create(snapshotsResource);
            try {
                dbSnapshotsResource.create();
            } catch (IOException e) {
                LOGGER.error("Failed to initialize parts store", e);
                System.exit(10);
            }
            ResourceFactory.registerSymlink("db/snapshots", dbSnapshotsResource);
        } else {
            LOGGER.info("Database snapshots are stored in a remote storage: " + snapshotsResource);
        }

        statementsResource = getSharedResource().resolve("statements", Resource.Type.DIRECTORY);
        if (statementsResource.isLocal()) {
            LOGGER.info("Database statements are stored in a RocksDB database: " + snapshotsResource);
            Resource dbStatementsResource = RocksDbResource.create(statementsResource);
            try {
                dbStatementsResource.create();
            } catch (IOException e) {
                LOGGER.error("Failed to initialize parts store", e);
                System.exit(10);
            }
            ResourceFactory.registerSymlink("db/statements", dbStatementsResource);
        } else {
            LOGGER.info("Database statements are stored in a remote storage: " + statementsResource);
        }
    }

    private Map<String, String> loadProperties(Schema schema) {
        Map<String, String> result = new HashMap<>();
        if (StringUtils.isNotEmpty(schema.getMappings())) {
            Properties properties = new Properties();
            try {
                properties.load(new StringReader(schema.getMappings()));
                properties.forEach((k, v) -> result.put(ObjectUtils.toString(k), ObjectUtils.toString(v)));
            } catch (IOException e) {
                LOGGER.error("Failed to load properties for database " + schema.getName(), e);
            }
        }
        return result;
    }

    private DataSource create(String id, String name, String url, String userName, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(userName);
        config.setPassword(password);
        config.setConnectionTimeout(CONNECT_TIMEOUT.toMillis());
        config.setIdleTimeout(AVAILABILITY_INTERVAL.multipliedBy(2).toMillis());
        HikariDataSource hikariDataSource = new HikariDataSource(config);
        return DataSource.create(id, name, hikariDataSource).withUri(URI.create(url))
                .withUserName(userName)
                .withPassword(password);
    }

    private int getNextSequence() {
        return resourceSequences.computeIfAbsent(LocalDate.now(), localDate -> {
            int start = 1;
            if (STARTUP.toLocalDate().equals(localDate)) {
                start = STARTUP.toLocalTime().toSecondOfDay();
            }
            return new AtomicInteger(start);
        }).getAndIncrement();
    }

}
