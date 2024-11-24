package net.microfalx.heimdall.rest.core.overview;

import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.heimdall.rest.api.RestService;
import net.microfalx.heimdall.rest.core.common.AbstractResultController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("OverviewSimulationResultController")
@DataSet(model = SimulationResult.class)
@RequestMapping("/rest/simulation/result")
public class SimulationResultController extends AbstractResultController<SimulationResult> {

    @Autowired
    private RestService restService;

    @Autowired
    private ContentService contentService;

    public RestService getRestService() {
        return restService;
    }

    public ContentService getContentService() {
        return contentService;
    }
}
