package net.microfalx.heimdall.rest.core.common;

import net.microfalx.bootstrap.content.Content;
import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.bootstrap.web.util.TableGenerator;
import net.microfalx.heimdall.rest.api.RestService;
import net.microfalx.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    protected RestService restService;

    @Autowired
    protected ContentService contentService;

    @GetMapping("/log/{id}")
    public String viewLog(@PathVariable("id") int id, Model model) throws IOException {
        Resource log = restService.getLog(id);
        model.addAttribute("log", log.loadAsString());
        return "rest/view_result::#log-modal";
    }

    @GetMapping("/data/{id}")
    public String viewData(@PathVariable("id") int id, Model model) throws IOException {
        Resource data = restService.getData(id);
        TableGenerator tableGenerator = new TableGenerator().setSmall(true).addRows(data);
        model.addAttribute("data", tableGenerator.generate());
        return "rest/view_result::#data-modal";
    }

    @GetMapping("/report/{id}")
    public String viewReport(@PathVariable("id") int id, Model model) throws IOException {
        Resource report = restService.getReport(id);
        Content content = Content.create(report);
        contentService.registerContent(content);
        model.addAttribute("content", content);
        return "rest/view_result::#report-modal";
    }
}
