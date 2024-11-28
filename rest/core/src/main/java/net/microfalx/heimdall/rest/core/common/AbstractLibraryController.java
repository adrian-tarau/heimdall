package net.microfalx.heimdall.rest.core.common;

import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.dataset.DataSet;
import net.microfalx.bootstrap.dataset.DataSetException;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.bootstrap.web.util.CodeEditor;
import net.microfalx.bootstrap.web.util.JsonResponse;
import net.microfalx.heimdall.rest.api.RestService;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static net.microfalx.heimdall.rest.api.Library.getNaturalId;
import static net.microfalx.heimdall.rest.api.RestConstants.SCRIPT_ATTR;

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

    /**
     * Returns a library by its identifier.
     *
     * @param id the identifier
     * @return a non-null instance
     */
    protected abstract T getLibrary(int id);

    /**
     * Saves the content of the library
     *
     * @param id       the identifier
     * @param resource the content
     */
    protected abstract void save(int id, Resource resource);

    @GetMapping("/design/{id}")
    public String design(@PathVariable("id") int id, Model model) throws IOException {
        T library = getLibrary(id);
        Resource resource = ResourceFactory.resolve(library.getResource()).withName("Design")
                .withMimeType(library.getType().getMimeType());
        return new CodeEditor<Integer>(contentService, resource, this).getEditorDialog(id, model);
    }

    @PostMapping("/design/{id}")
    @ResponseBody()
    public JsonResponse<?> design(@PathVariable("id") int id, @RequestBody String content, Model model) throws IOException {
        Resource resource = register(Resource.text(content));
        save(id, resource);
        return JsonResponse.success();
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

    @Override
    protected boolean beforeEdit(DataSet<T, Field<T>, Integer> dataSet, Model controllerModel, T dataSetModel) {
        if (dataSetModel.getProject() != null) setReadOnlyExcept("name", "description", "tags", "override");
        return super.beforeEdit(dataSet, controllerModel, dataSetModel);
    }

    @Override
    protected boolean beforePersist(DataSet<T, Field<T>, Integer> dataSet, T model, State state) {
        Resource resolve = ResourceFactory.resolve(model.getResource());
        if (state == State.ADD) {
            model.setNaturalId(getNaturalId(model.getType(), resolve, model.getProject() != null ? model.getProject().getNaturalId() : null));
        }
        return super.beforePersist(dataSet, model, state);
    }

    @Override
    protected void afterPersist(DataSet<T, Field<T>, Integer> dataSet, T model, State state) {
        super.afterPersist(dataSet, model, state);
        restService.reload();
    }
}
