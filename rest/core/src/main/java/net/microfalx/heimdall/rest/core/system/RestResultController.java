package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.content.Content;
import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.heimdall.rest.api.RestService;
import net.microfalx.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller("SystemRestResultController")
@DataSet(model = RestResult.class)
@RequestMapping("/system/rest/result")
public class RestResultController extends DataSetController<RestResult,Long> {

    @Autowired
    private RestService restService;

    @Autowired
    private ContentService contentService;

    @GetMapping("/log/{id}")
    public String viewLog(@PathVariable("id") int id, Model model) throws IOException {
        Resource log = restService.getLog(id);
        model.addAttribute("log", log.loadAsString());
        return "rest/view_result::#log-modal";
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
