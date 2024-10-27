package net.microfalx.heimdall.rest.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.heimdall.rest.api.Library;
import net.microfalx.heimdall.rest.core.system.RestLibrary;
import net.microfalx.heimdall.rest.core.system.RestLibraryRepository;
import net.microfalx.heimdall.rest.core.system.RestSimulation;
import net.microfalx.heimdall.rest.core.system.RestSimulationRepository;
import net.microfalx.lang.CollectionUtils;

public class RestPersistence extends ApplicationContextSupport {

    RestLibrary execute(Library library) {
        RestLibraryRepository restLibraryRepository = getBean(RestLibraryRepository.class);
        RestLibrary jpaLibrary = new RestLibrary();
        //jpaLibrary.setProject(execute(library.getProject()));
        jpaLibrary.setType(library.getType());
        jpaLibrary.setName(library.getName());
        jpaLibrary.setDescription(library.getDescription());
        jpaLibrary.setResource(library.getResource().toURI().toASCIIString());
        jpaLibrary.setTags(CollectionUtils.setToString(library.getTags()));
        return restLibraryRepository.save(jpaLibrary);
    }

    RestSimulation execute(net.microfalx.heimdall.rest.api.Simulation simulation) {
        RestSimulationRepository simulationRepository = getBean(RestSimulationRepository.class);
        RestSimulation jpaSimulation = new RestSimulation();
        //jpaSimulation.setProject(execute(simulation.getProject()));
        jpaSimulation.setDescription(simulation.getDescription());
        jpaSimulation.setResource(simulation.getResource().toURI().toASCIIString());
        jpaSimulation.setType(simulation.getType());
        jpaSimulation.setNaturalId(simulation.getId());
        jpaSimulation.setTags(CollectionUtils.setToString(simulation.getTags()));
        jpaSimulation.setName(simulation.getName());
        return simulationRepository.save(jpaSimulation);
    }

}




