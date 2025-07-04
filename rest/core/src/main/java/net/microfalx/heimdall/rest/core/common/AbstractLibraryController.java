package net.microfalx.heimdall.rest.core.common;

import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.dataset.DataSet;
import net.microfalx.bootstrap.dataset.DataSetException;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.component.Item;
import net.microfalx.bootstrap.web.component.Menu;
import net.microfalx.bootstrap.web.component.Separator;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.bootstrap.web.util.CodeEditor;
import net.microfalx.bootstrap.web.util.JsonResponse;
import net.microfalx.heimdall.rest.api.Project;
import net.microfalx.heimdall.rest.api.RestService;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collection;

import static net.microfalx.heimdall.rest.api.Library.getNaturalId;
import static net.microfalx.heimdall.rest.api.RestConstants.SCRIPT_ATTR;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all controllers dealing with a library.
 *
 * @param <T> the library type
 */
public abstract class AbstractLibraryController<T extends AbstractLibrary> extends DataSetController<T, Integer> {

    @Autowired
    protected RestService restService;

    @Autowired
    protected ContentService contentService;

    @Autowired
    private TaskExecutor executor;

    /**
     * Returns a library by its identifier.
     *
     * @param id the identifier
     * @return a non-null instance
     */
    protected abstract T getLibrary(int id);

    /**
     * Extracts the history of a library.
     *
     * @param library the library
     * @return a list of history objects
     */
    protected abstract Collection<?> extractHistory(T library);

    /**
     * Saves the content of the library
     *
     * @param id       the identifier
     * @param resource the content
     */
    protected abstract void save(int id, Resource resource);

    /**
     * Returns the name used in labels and descriptions.
     *
     * @return a non-null instance
     */
    protected abstract String getName();

    @GetMapping("/design/{id}")
    public String design(@PathVariable("id") int id, Model model) throws IOException {
        T library = getLibrary(id);
        Resource resource = ResourceFactory.resolve(library.getResource()).withName("Design")
                .withMimeType(library.getType().getMimeType());
        return new CodeEditor<Integer>(contentService, resource, this).getDialog(id, model);
    }

    @PostMapping("/design/{id}")
    @ResponseBody()
    public JsonResponse<?> design(@PathVariable("id") int id, @RequestBody String content, Model model) throws IOException {
        Resource resource = register(Resource.text(content));
        save(id, resource);
        return JsonResponse.success();
    }

    @GetMapping("/view/history/{id}")
    public String history(@PathVariable("id") int id, Model model) throws IOException {
        T library = getLibrary(id);
        model.addAttribute("library", library);
        model.addAttribute("items", extractHistory(library));
        return "rest/view_simulation_or_library_history::#history-modal";
    }

    @Override
    protected void updateActions(Menu menu) {
        super.updateActions(menu);
        menu.add(new Separator());
        String name = StringUtils.uncapitalizeFirst(getName());
        menu.add(new Item().setAction("rest.library.design").setText("Design").setIcon("fa-solid fa-pen-nib").setDescription("Design the " + name + " script"));
        menu.add(new Item().setAction("rest.library.history").setText("History").setIcon("fa-solid fa-timeline").setDescription("Show the history of the " + name));
    }

    protected final Simulation discover(Resource resource) {
        try {
            return restService.discover(resource);
        } catch (Exception e) {
            throw new DataSetException("Invalid library type '" + resource.getName() + "'");
        }
    }

    protected final Resource register(Resource resource) throws IOException {
        return restService.registerResource(resource.withAttribute(SCRIPT_ATTR, Boolean.TRUE));
    }


    protected final void reloadProject(String id) {
        requireNonNull(id);
        Project project = restService.getProject(id);
        executor.execute(() -> restService.reload(project));
    }

    @Override
    protected void beforeEdit(DataSet<T, Field<T>, Integer> dataSet, Model controllerModel, T dataSetModel) {
        if (dataSetModel.getProject().getType() != Project.Type.NONE) {
            setReadOnlyExcept("name", "description", "tags", "override");
        }
    }

    @Override
    protected void beforePersist(DataSet<T, Field<T>, Integer> dataSet, T model, State state) {
        Resource resolve = ResourceFactory.resolve(model.getResource());
        if (state == State.ADD) {
            model.setNaturalId(getNaturalId(model.getType(), resolve, model.getProject() != null ? model.getProject().getNaturalId() : null));
        }
    }

    @Override
    protected void afterPersist(DataSet<T, Field<T>, Integer> dataSet, T model, State state) {
        executor.execute(restService::reload);
    }

}
