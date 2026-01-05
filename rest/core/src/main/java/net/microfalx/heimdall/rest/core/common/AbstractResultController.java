package net.microfalx.heimdall.rest.core.common;

import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.heimdall.rest.api.RestService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

/**
 * Base class for all controllers dealing with results.
 *
 * @param <R> the result type
 */
public abstract class AbstractResultController<R extends AbstractResult> extends DataSetController<R, Long> {

    protected final RestService restService;
    protected final ContentService contentService;

    public AbstractResultController(DataSetService dataSetService, RestService restService, ContentService contentService) {
        super(dataSetService);
        this.restService = restService;
        this.contentService = contentService;
    }

    @GetMapping("/log/{id}")
    public String viewLog(@PathVariable("id") int id, Model model) throws IOException {
        return getHelper(id, model).viewLog();
    }

    @GetMapping("/data/{id}")
    public String viewData(@PathVariable("id") int id, Model model) throws IOException {
        return getHelper(id, model).viewData();
    }

    @GetMapping("/report/{id}")
    public String viewReport(@PathVariable("id") int id, Model model) throws IOException {
        return getHelper(id, model).viewReport();
    }

    private SimulationControllerHelper getHelper(int id, Model model) {
        return new SimulationControllerHelper(restService, contentService, model).setResult(id);
    }
}
