package net.microfalx.heimdall.rest.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.heimdall.rest.api.Library;
import net.microfalx.heimdall.rest.api.Project;
import net.microfalx.heimdall.rest.api.Schedule;
import net.microfalx.heimdall.rest.api.Simulation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableCollection;

class RestCache extends ApplicationContextSupport {

    private volatile Map<String, Project> projects = new HashMap<>();
    private volatile Map<String, Simulation> simulations = new HashMap<>();
    private volatile Map<String, Schedule> schedules = new HashMap<>();
    private volatile Map<String, Library> libraries = new HashMap<>();

    Collection<Project> getProjects() {
        return unmodifiableCollection(projects.values());
    }

    Collection<Simulation> getSimulations() {
        return unmodifiableCollection(simulations.values());
    }

    Collection<Schedule> getSchedules() {
        return unmodifiableCollection(schedules.values());
    }

    Collection<Library> getLibraries() {
        return unmodifiableCollection(libraries.values());
    }

    void load() {

    }
}
