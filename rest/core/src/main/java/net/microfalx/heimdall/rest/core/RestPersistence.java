package net.microfalx.heimdall.rest.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.jdbc.jpa.NaturalIdEntityUpdater;
import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.heimdall.rest.api.Library;
import net.microfalx.heimdall.rest.api.Project;
import net.microfalx.heimdall.rest.api.SimulationException;
import net.microfalx.heimdall.rest.core.system.*;
import net.microfalx.lang.CollectionUtils;

public class RestPersistence extends ApplicationContextSupport {

    void save(Project project) {
        NaturalIdEntityUpdater<RestProject, Integer> restLibraryUpdater = getUpdater(RestProjectRepository.class);
        RestProject jpaProject = new RestProject();
        jpaProject.setName(project.getName());
        jpaProject.setType(project.getType());
        jpaProject.setNaturalId(project.getId());
        jpaProject.setUri(project.getUri().toASCIIString());
        jpaProject.setUserName(project.getUserName());
        jpaProject.setPassword(project.getPassword());
        jpaProject.setToken(project.getToken());
        jpaProject.setTags(CollectionUtils.setToString(project.getTags()));
        jpaProject.setLibraryPath(project.getLibraryPath());
        jpaProject.setSimulationPath(project.getSimulationPath());
        jpaProject.setDescription(project.getDescription());
        restLibraryUpdater.findByNaturalIdAndUpdate(jpaProject);
    }

    void save(Library library) {
        NaturalIdEntityUpdater<RestLibrary, Integer> restLibraryUpdater = getUpdater(RestLibraryRepository.class);
        RestLibrary jpaLibrary = new RestLibrary();
        jpaLibrary.setName(library.getName());
        jpaLibrary.setType(library.getType());
        jpaLibrary.setNaturalId(library.getId());
        jpaLibrary.setProject(loadProject(library.getProject()));
        jpaLibrary.setResource(library.getResource().toURI().toASCIIString());
        jpaLibrary.setPath(library.getPath());
        jpaLibrary.setTags(CollectionUtils.setToString(library.getTags()));
        jpaLibrary.setDescription(library.getDescription());
        restLibraryUpdater.findByNaturalIdAndUpdate(jpaLibrary);
    }

    void save(net.microfalx.heimdall.rest.api.Simulation simulation) {
        NaturalIdEntityUpdater<RestSimulation, Integer> updater = getUpdater(RestSimulationRepository.class);
        RestSimulation jpaSimulation = new RestSimulation();
        jpaSimulation.setName(simulation.getName());
        jpaSimulation.setNaturalId(simulation.getId());
        jpaSimulation.setType(simulation.getType());
        jpaSimulation.setTimeout((int) simulation.getTimeout().toSeconds());
        jpaSimulation.setProject(loadProject(simulation.getProject()));
        jpaSimulation.setResource(simulation.getResource().toURI().toASCIIString());
        jpaSimulation.setPath(simulation.getPath());
        jpaSimulation.setOverride(true);
        jpaSimulation.setTags(CollectionUtils.setToString(simulation.getTags()));
        jpaSimulation.setDescription(simulation.getDescription());
        updater.findByNaturalIdAndUpdate(jpaSimulation);
    }

    private <M, ID> NaturalIdEntityUpdater<M, ID> getUpdater(Class<? extends NaturalJpaRepository<M, ID>> repositoryClass) {
        NaturalJpaRepository<M, ID> repository = getBean(repositoryClass);
        NaturalIdEntityUpdater<M, ID> updater = new NaturalIdEntityUpdater<>(getBean(MetadataService.class), repository);
        updater.setApplicationContext(getApplicationContext());
        return updater;
    }

    private RestProject loadProject(Project project) {
        if (project == null) return null;
        return getBean(RestProjectRepository.class).findByNaturalId(project.getId())
                .orElseThrow(() -> new SimulationException("A project with identifier '" + project.getId() + "' is not registered"));
    }

}




