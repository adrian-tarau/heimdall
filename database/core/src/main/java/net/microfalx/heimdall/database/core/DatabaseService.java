package net.microfalx.heimdall.database.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.microfalx.bootstrap.jdbc.support.DataSource;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service("heimdallDatabaseService")
public class DatabaseService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseService.class);

    @Autowired
    private net.microfalx.bootstrap.jdbc.support.DatabaseService databaseService;

    @Autowired
    private DatabaseProperties databaseProperties;

    @Autowired
    private DatabaseManageRepository databaseManageRepository;

    @Autowired
    private TaskScheduler taskScheduler;

    net.microfalx.bootstrap.jdbc.support.DatabaseService getDatabaseService() {
        return databaseService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        reload();
        scheduleTasks();
    }

    public void reload() {
        LOGGER.info("Register databases (schemas)");
        for (DatabaseManage databaseManage : databaseManageRepository.findAll()) {
            String id = StringUtils.toIdentifier(databaseManage.getName() + "_" + databaseManage.getType());
            try {
                DataSource dataSource = create(id, databaseManage.getName(), databaseManage.getUrl(), databaseManage.getUsername(), databaseManage.getPassword());
                dataSource = dataSource.withProperties(loadProperties(databaseManage))
                        .withDescription(databaseManage.getDescription());
                databaseService.registerDataSource(dataSource);
            } catch (Exception e) {
                LOGGER.error("Failed to register data source for database " + databaseManage.getName(), e);
            }
        }
    }

    private void scheduleTasks() {
        taskScheduler.schedule(new DatabaseSnapshotsTask(this), new PeriodicTrigger(databaseProperties.getInterval()));
    }

    private Map<String, String> loadProperties(DatabaseManage databaseManage) {
        Map<String, String> result = new HashMap<>();
        if (StringUtils.isNotEmpty(databaseManage.getMappings())) {
            Properties properties = new Properties();
            try {
                properties.load(new StringReader(databaseManage.getMappings()));
                properties.forEach((k, v) -> result.put(ObjectUtils.toString(k), ObjectUtils.toString(v)));
            } catch (IOException e) {
                LOGGER.error("Failed to load properties for database " + databaseManage.getName(), e);
            }
        }
        return result;
    }

    private DataSource create(String id, String name, String url, String userName, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(userName);
        config.setPassword(password);
        HikariDataSource hikariDataSource = new HikariDataSource(config);
        return DataSource.create(id, name, hikariDataSource).withUri(URI.create(url))
                .withUserName(userName)
                .withPassword(password);
    }

}
