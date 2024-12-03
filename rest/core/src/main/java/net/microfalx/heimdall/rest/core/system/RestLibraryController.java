package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.content.Content;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.component.Item;
import net.microfalx.bootstrap.web.component.Menu;
import net.microfalx.bootstrap.web.component.Separator;
import net.microfalx.heimdall.rest.api.Library;
import net.microfalx.heimdall.rest.api.Project;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.core.common.AbstractLibraryController;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

import static net.microfalx.heimdall.rest.core.RestUtils.prepareContent;

@Controller("SystemLibraryController")
@DataSet(model = RestLibrary.class, timeFilter = false, canAdd = false, canUpload = true,
        viewTemplate = "rest/view_simulation_or_library", viewClasses = "modal-xl")
@RequestMapping("/system/rest/library")
@Help("rest/system/library")
public class RestLibraryController extends AbstractLibraryController<RestLibrary> {

    @Autowired
    private RestLibraryRepository repository;

    @Override
    protected RestLibrary getLibrary(int id) {
        return repository.findById(id).orElseThrow();
    }

    @Override
    protected void save(int id, Resource resource) {
        Library library = restService.getLibrary(Integer.toString(id));
        library = library.withResource(resource).withOverride(true);
        restService.registerLibrary(library);
    }

    @Override
    protected void updateActions(Menu menu) {
        super.updateActions(menu);
        menu.add(new Separator());
        menu.add(new Item().setAction("rest.library.design").setText("Design").setIcon("fa-solid fa-pen-nib").setDescription("Design the library"));
        menu.add(new Item().setAction("rest.library.history").setText("History").setIcon("fa-solid fa-timeline").setDescription("Show the history of the library"));
    }

    @Override
    protected void beforeView(net.microfalx.bootstrap.dataset.DataSet<RestLibrary, Field<RestLibrary>, Integer> dataSet, Model controllerModel, RestLibrary dataSetModel) {
        super.beforeView(dataSet, controllerModel, dataSetModel);
        Content content = prepareContent(contentService, dataSetModel.getNaturalId(), "simulation", dataSetModel.getResource(), dataSetModel.getType());
        if (content != null) {
            controllerModel.addAttribute("content", content);
        }
    }

    @Override
    protected boolean beforeDelete(net.microfalx.bootstrap.dataset.DataSet<RestLibrary, Field<RestLibrary>, Integer> dataSet, Model controllerModel, RestLibrary dataSetModel) {
        if (dataSetModel.isGlobal()) return cancel(controllerModel, "A global library cannot be deleted");
        if (dataSetModel.getProject().getType() != Project.Type.NONE) return cancel(controllerModel,"A library hosted in VCS cannot be deleted");
        return super.beforeDelete(dataSet, controllerModel, dataSetModel);
    }

    @Override
    protected boolean beforeEdit(net.microfalx.bootstrap.dataset.DataSet<RestLibrary, Field<RestLibrary>, Integer> dataSet, Model controllerModel, RestLibrary dataSetModel) {
        if (dataSetModel.isGlobal()) return cancel(controllerModel, "A global library cannot be updated");
        return super.beforeEdit(dataSet, controllerModel, dataSetModel);
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<RestLibrary, Field<RestLibrary>, Integer> dataSet, RestLibrary model, State state) {
        super.afterPersist(dataSet, model, state);
        if (model.getProject().getType() != Project.Type.NONE && !model.isOverride()) {
            reloadProject(model.getProject().getNaturalId());
        }
    }

    @Override
    protected void upload(net.microfalx.bootstrap.dataset.DataSet<RestLibrary, Field<RestLibrary>, Integer> dataSet, Model model, Resource resource) {
        Simulation simulation = discover(resource);
        try {
            Resource storedResource = register(resource);
            Library.Builder builder = new Library.Builder(simulation.getId()).resource(storedResource).type(simulation.getType())
                    .project(Project.DEFAULT).path(resource.getFileName());
            builder.tags(simulation.getTags()).name(simulation.getName()).description(simulation.getDescription());
            restService.registerLibrary(builder.build());
        } catch (IOException e) {
            ExceptionUtils.throwException(e);
        }
    }
}
