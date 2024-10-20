package net.microfalx.heimdall.rest.core;


import lombok.extern.slf4j.Slf4j;
import net.microfalx.heimdall.rest.api.Library;
import net.microfalx.heimdall.rest.api.RestService;
import net.microfalx.heimdall.rest.api.Schedule;
import net.microfalx.heimdall.rest.api.Simulation;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

import static java.util.Collections.unmodifiableCollection;

@Service
@Slf4j
public class RestServiceImpl implements RestService, InitializingBean {

    private volatile Collection<Simulation> simulations = Collections.emptyList();
    private volatile Collection<Schedule> schedules = Collections.emptyList();
    private volatile Collection<Library> libraries = Collections.emptyList();

    @Autowired
    private RestProperties properties;

    @Autowired
    private ApplicationContext applicationContext;

    private final RestPersistence persistence = new RestPersistence();

    @Override
    public Collection<Simulation> getSimulations() {
        return unmodifiableCollection(simulations);
    }

    @Override
    public Collection<Schedule> getSchedules() {
        return unmodifiableCollection(schedules);
    }

    @Override
    public Collection<Library> getLibraries() {
        return unmodifiableCollection(libraries);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        persistence.setApplicationContext(applicationContext);
        loadSimulations();
    }

    private void loadSimulations() {

    }
}
