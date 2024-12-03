package net.microfalx.heimdall.rest.core.common;

import net.microfalx.bootstrap.content.Content;
import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.web.util.TableGenerator;
import net.microfalx.heimdall.rest.api.RestService;
import net.microfalx.heimdall.rest.api.Simulator;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
import org.springframework.ui.Model;

import java.io.IOException;

import static net.microfalx.bootstrap.web.controller.PageController.MESSAGE_ATTR;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;


/**
 * A helper used by controllers to display various information related to simulations (or their results).
 */
public class SimulationControllerHelper {

    private final RestService restService;
    private final ContentService contentService;
    private final Model model;

    private Integer id;
    private Simulator simulator;

    public SimulationControllerHelper(RestService restService, ContentService contentService, Model model) {
        requireNonNull(restService);
        requireNonNull(contentService);
        this.restService = restService;
        this.contentService = contentService;
        this.model = model;
    }

    public SimulationControllerHelper setResult(int id) {
        this.id = id;
        return this;
    }

    public SimulationControllerHelper setSimulator(Simulator simulator) {
        this.simulator = simulator;
        return this;
    }

    public String viewLog() throws IOException {
        Resource log;
        if (id != null) {
            log = restService.getLog(id);
        } else {
            log = simulator.getLogs();
        }
        model.addAttribute("log", log.loadAsString());
        return "rest/view_result::#log-modal";
    }

    public String viewData() throws IOException {
        Resource data;
        if (id != null) {
            data = restService.getData(id);
        } else {
            data = simulator.getData();
        }
        TableGenerator tableGenerator = new TableGenerator().setSmall(true).setStrict(false).setLinks(true).addRows(data);
        model.addAttribute("data", tableGenerator.generate());
        return "rest/view_result::#data-modal";
    }

    public String viewReport() throws IOException {
        Resource report;
        if (id != null) {
            report = restService.getReport(id);
        } else {
            report = restService.getReport(simulator);
        }
        Content content = Content.create(report);
        contentService.registerContent(content);
        String dialogCss;
        if (!MimeType.get(report.getMimeType()).isText()) {
            dialogCss = "modal-sm";
            model.addAttribute(MESSAGE_ATTR, "The report can be viewed in the browser.");
        } else {
            dialogCss = "modal-xl";
        }
        model.addAttribute("css", dialogCss);
        model.addAttribute("content", content);
        return "rest/view_result::#report-modal";
    }
}
