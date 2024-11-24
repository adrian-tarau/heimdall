package net.microfalx.heimdall.rest.core.common;

import net.microfalx.bootstrap.content.Content;
import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.heimdall.rest.api.RestService;
import net.microfalx.resource.Resource;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

public abstract class AbstractResultController<R extends AbstractResult> extends DataSetController<R, Long> {

    protected abstract RestService getRestService();

    protected abstract ContentService getContentService();

    @GetMapping("/log/{id}")
    public String viewLog(@PathVariable("id") int id, Model model) throws IOException {
        Resource log = getRestService().getLog(id);
        model.addAttribute("log", log.loadAsString());
        return "rest/view_result::#log-modal";
    }

    @GetMapping("/data/{id}")
    public String viewData(@PathVariable("id") int id, Model model) throws IOException {
        Resource data = getRestService().getData(id);
        model.addAttribute("data", data.loadAsString());
        return "rest/view_result::#data-modal";
    }

    @GetMapping("/report/{id}")
    public String viewReport(@PathVariable("id") int id, Model model) throws IOException {
        Resource report = getRestService().getReport(id);
        Content content = Content.create(report);
        getContentService().registerContent(content);
        model.addAttribute("content", content);
        return "rest/view_result::#report-modal";
    }
}
