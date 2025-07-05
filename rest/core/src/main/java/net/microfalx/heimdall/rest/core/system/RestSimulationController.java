package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.content.Content;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.heimdall.rest.api.Project;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.core.common.AbstractLibraryController;
import net.microfalx.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.Collection;

import static net.microfalx.heimdall.rest.core.RestUtils.prepareContent;
import static net.microfalx.lang.ExceptionUtils.rethrowException;

@Controller("SystemSimulationController")
@DataSet(model = RestSimulation.class, timeFilter = false, canAdd = false, canUpload = true,
        viewTemplate = "rest/view_simulation_or_library", viewClasses = "modal-xl")
@RequestMapping("/system/rest/simulation")
@Help("rest/system/simulation")
public class RestSimulationController extends AbstractLibraryController<RestSimulation> {

    @Autowired
    private RestSimulationRepository repository;

    @Autowired
    private RestSimulationHistoryRepository restSimulationHistoryRepository;

    @Override
    protected RestSimulation getLibrary(int id) {
        return repository.findById(id).orElseThrow();
    }

    @Override
    protected Collection<?> extractHistory(RestSimulation library) {
        return restSimulationHistoryRepository.findAllByRestSimulation(library);
    }

    @Override
    protected String getName() {
        return "Simulation";
    }

    @Override
    protected void save(int id, Resource resource) {
        Simulation simulation = restService.getSimulation(Integer.toString(id));
        simulation = (Simulation) simulation.withResource(resource).withOverride(true);
        restService.registerSimulation(simulation);
    }

    @Override
    protected void beforeView(net.microfalx.bootstrap.dataset.DataSet<RestSimulation, Field<RestSimulation>, Integer> dataSet, Model controllerModel, RestSimulation dataSetModel) {
        super.beforeView(dataSet, controllerModel, dataSetModel);
        Content content = prepareContent(contentService, dataSetModel.getNaturalId(), "simulation", dataSetModel.getResource(), dataSetModel.getType());
        if (content != null) {
            controllerModel.addAttribute("content", content);
        }
    }

    @Override
    protected void beforeDelete(net.microfalx.bootstrap.dataset.DataSet<RestSimulation, Field<RestSimulation>, Integer> dataSet, Model controllerModel, RestSimulation dataSetModel) {
        if (dataSetModel.getProject().getType() != Project.Type.NONE) {
            cancel("A simulation hosted in VCS cannot be deleted");
        }
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<RestSimulation, Field<RestSimulation>, Integer> dataSet, RestSimulation model, State state) {
        super.afterPersist(dataSet, model, state);
        if (model.getProject().getType() != Project.Type.NONE && !model.isOverride()) {
            reloadProject(model.getProject().getNaturalId());
        }
    }

    @Override
    protected void upload(net.microfalx.bootstrap.dataset.DataSet<RestSimulation, Field<RestSimulation>, Integer> dataSet, Model model, Resource resource) {
        Simulation simulation = discover(resource);
        try {
            Resource storedResource = register(resource);
            Simulation.Builder builder = new Simulation.Builder(simulation);
            builder.project(Project.DEFAULT).resource(storedResource).path(resource.getFileName());
            restService.registerSimulation(builder.build());
        } catch (IOException e) {
            rethrowException(e);
        }
    }

}
