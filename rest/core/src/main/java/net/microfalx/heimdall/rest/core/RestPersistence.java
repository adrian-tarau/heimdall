package net.microfalx.heimdall.rest.core;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.jdbc.jpa.NaturalIdEntityUpdater;
import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.heimdall.rest.api.Library;
import net.microfalx.heimdall.rest.api.Project;
import net.microfalx.heimdall.rest.api.SimulationException;
import net.microfalx.heimdall.rest.core.system.*;
import net.microfalx.lang.CollectionUtils;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import net.microfalx.resource.ResourceUtils;

import java.io.IOException;

@Slf4j
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
        RestLibraryRepository restLibraryRepository = getBean(RestLibraryRepository.class);
        NaturalIdEntityUpdater<RestLibrary, Integer> restLibraryUpdater = getUpdater(RestLibraryRepository.class);
        int version = 1;
        RestLibrary previousJpaLibrary = restLibraryRepository.findByNaturalId(library.getId()).orElse(null);
        if (previousJpaLibrary != null) {
            version = previousJpaLibrary.getVersion() + 1;
            if (hasResourceChanged(previousJpaLibrary.getResource(), library.getResource())) {
                saveHistory(previousJpaLibrary);
            }
        }
        RestLibrary jpaLibrary = new RestLibrary();
        jpaLibrary.setName(library.getName());
        jpaLibrary.setType(library.getType());
        jpaLibrary.setNaturalId(library.getId());
        jpaLibrary.setProject(loadProject(library.getProject()));
        jpaLibrary.setResource(library.getResource().toURI().toASCIIString());
        jpaLibrary.setPath(library.getPath());
        jpaLibrary.setTags(CollectionUtils.setToString(library.getTags()));
        jpaLibrary.setDescription(library.getDescription());
        jpaLibrary.setVersion(version);
        restLibraryUpdater.findByNaturalIdAndUpdate(jpaLibrary);
    }

    void saveHistory(RestLibrary restLibrary) {
        RestLibraryHistory restLibraryHistory = new RestLibraryHistory();
        restLibraryHistory.setRestLibrary(restLibrary);
        restLibraryHistory.setVersion(restLibrary.getVersion());
        restLibraryHistory.setResource(restLibrary.getResource());
        restLibraryHistory.setCreatedAt(restLibrary.getCreatedAt());
        restLibraryHistory.setModifiedAt(restLibrary.getModifiedAt());
        RestLibraryHistoryRepository restSimulationHistoryRepository = getBean(RestLibraryHistoryRepository.class);
        restSimulationHistoryRepository.save(restLibraryHistory);
    }


    void save(net.microfalx.heimdall.rest.api.Simulation simulation) {
        RestSimulationRepository restSimulationRepository = getBean(RestSimulationRepository.class);
        int version = 1;
        RestSimulation previousJpaSimulation = restSimulationRepository.findByNaturalId(simulation.getId()).orElse(null);
        if (previousJpaSimulation != null) {
            version = previousJpaSimulation.getVersion() + 1;
            if (hasResourceChanged(previousJpaSimulation.getResource(), simulation.getResource())) {
                saveHistory(previousJpaSimulation);
            }
        }
        NaturalIdEntityUpdater<RestSimulation, Integer> updater = getUpdater(RestSimulationRepository.class);
        RestSimulation jpaSimulation = new RestSimulation();
        jpaSimulation.setName(simulation.getName());
        jpaSimulation.setNaturalId(simulation.getId());
        jpaSimulation.setType(simulation.getType());
        jpaSimulation.setTimeout((int) simulation.getTimeout().toSeconds());
        jpaSimulation.setProject(loadProject(simulation.getProject()));
        jpaSimulation.setResource(simulation.getResource().toURI().toASCIIString());
        jpaSimulation.setPath(simulation.getPath());
        jpaSimulation.setVersion(version);
        jpaSimulation.setTags(CollectionUtils.setToString(simulation.getTags()));
        jpaSimulation.setDescription(simulation.getDescription());
        updater.findByNaturalIdAndUpdate(jpaSimulation);
    }

    void saveHistory(RestSimulation jpaSimulation) {
        RestSimulationHistory restSimulationHistory = new RestSimulationHistory();
        restSimulationHistory.setRestSimulation(jpaSimulation);
        restSimulationHistory.setVersion(jpaSimulation.getVersion());
        restSimulationHistory.setResource(jpaSimulation.getResource());
        restSimulationHistory.setCreatedAt(jpaSimulation.getCreatedAt());
        restSimulationHistory.setModifiedAt(jpaSimulation.getModifiedAt());
        RestSimulationHistoryRepository restSimulationHistoryRepository = getBean(RestSimulationHistoryRepository.class);
        restSimulationHistoryRepository.save(restSimulationHistory);
    }

    private boolean hasResourceChanged(String previousUri, Resource currentResource) {
        Resource previousResource = ResourceFactory.resolve(previousUri);
        try {
            return !ResourceUtils.hasSameContent(previousResource, currentResource);
        } catch (IOException e) {
            LOGGER.error("Failed to compare resources '{}' with '{}'", previousUri, currentResource.toURI());
            return false;
        }
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




