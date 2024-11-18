package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.content.Content;
import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.component.Item;
import net.microfalx.bootstrap.web.component.Menu;
import net.microfalx.bootstrap.web.component.Separator;
import net.microfalx.heimdall.rest.api.Library;
import net.microfalx.heimdall.rest.api.RestService;
import net.microfalx.heimdall.rest.api.Simulation;
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
public class RestLibraryController extends AbstractLibraryController<RestLibrary> {

    @Autowired
    private RestService restService;

    @Autowired
    private ContentService contentService;

    @Override
    public RestService getRestService() {
        return restService;
    }

    @Override
    public ContentService getContentService() {
        return contentService;
    }

    @Override
    protected void updateActions(Menu menu) {
        super.updateActions(menu);
        menu.add(new Separator());
        menu.add(new Item().setAction("rest.library.design").setText("Design").setIcon("fa-solid fa-pen-nib").setDescription("Design the library"));
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
    protected void upload(net.microfalx.bootstrap.dataset.DataSet<RestLibrary, Field<RestLibrary>, Integer> dataSet, Model model, Resource resource) {
        Simulation simulation = discover(resource);
        try {
            Resource storedResource = register(resource);
            Library.Builder builder = new Library.Builder(simulation.getId()).resource(storedResource).type(simulation.getType())
                    .path(resource.getFileName());
            builder.tags(simulation.getTags()).name(simulation.getName()).description(simulation.getDescription());
            restService.registerLibrary(builder.build());
        } catch (IOException e) {
            ExceptionUtils.throwException(e);
        }
    }
}
