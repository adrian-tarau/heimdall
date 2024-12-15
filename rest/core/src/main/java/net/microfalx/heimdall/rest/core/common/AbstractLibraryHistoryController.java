package net.microfalx.heimdall.rest.core.common;

import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.bootstrap.web.util.CodeEditor;
import net.microfalx.bootstrap.web.util.JsonResponse;
import net.microfalx.heimdall.rest.api.RestService;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

/**
 * Base controller class for all histories.
 *
 * @param <T> the type of history
 */
public abstract class AbstractLibraryHistoryController<T extends AbstractLibraryHistory, L extends AbstractLibrary> extends DataSetController<T, Integer> {

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
    protected abstract T getHistory(int id);

    /**
     * Returns the library referenced a  history entry
     *
     * @param history the history
     * @return a non-null instance
     */
    protected abstract L getLibrary(T history);

    /**
     * Persist the reverted library
     *
     * @param library the reverted library
     */
    protected abstract void save(L library);

    @GetMapping("/view/{id}")
    public String view(@PathVariable("id") int id, Model model) throws IOException {
        T history = getHistoryOrFail(id);
        AbstractLibrary library = getLibrary(history);
        Resource resource = ResourceFactory.resolve(history.getResource()).withName("History")
                .withMimeType(library.getType().getMimeType());
        return new CodeEditor<Integer>(contentService, resource, this).setReadOnly(true).getDialog(id, model);
    }

    @GetMapping("/revert/{id}")
    @ResponseBody
    public JsonResponse<?> revert(@PathVariable("id") int id, Model model) throws IOException {
        T history = getHistoryOrFail(id);
        L library = getLibrary(history);
        library.setResource(history.getResource());
        save(library);
        return JsonResponse.success("The " + getTitle() + " was restored to version " + history.getVersion());
    }

    private T getHistoryOrFail(int id) {
        T history = getHistory(id);
        if (history == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "History not found " + id);
        return history;
    }

}
