package net.microfalx.heimdall.rest.core;


import lombok.extern.slf4j.Slf4j;
import net.microfalx.heimdall.rest.api.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@Slf4j
public class RestServiceImpl implements RestService, InitializingBean {

    private volatile RestCache cache = new RestCache();

    @Autowired
    private RestProperties properties;

    @Autowired
    private ApplicationContext applicationContext;

    private final RestPersistence persistence = new RestPersistence();

    @Override
    public Collection<Project> getProjects() {
        return cache.getProjects();
    }

    @Override
    public Collection<Simulation> getSimulations() {
        return cache.getSimulations();
    }

    @Override
    public void registerSimulation(Simulation simulation) {

    }

    @Override
    public Collection<Schedule> getSchedules() {
        return cache.getSchedules();
    }

    @Override
    public Collection<Library> getLibraries() {
        return cache.getLibraries();
    }

    @Override
    public void registerLibrary(Library library) {

    }

    @Override
    public void reload() {
        RestCache cache = new RestCache();
        cache.setApplicationContext(applicationContext);
        cache.load();
        this.cache = cache;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        persistence.setApplicationContext(applicationContext);
        this.reload();
    }

}
