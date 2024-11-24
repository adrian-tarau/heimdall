package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.heimdall.rest.api.RestService;
import net.microfalx.heimdall.rest.core.common.AbstractResultController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemRestResultController")
@DataSet(model = RestResult.class)
@RequestMapping("/system/rest/result")
@Help("rest/system/result")
public class RestResultController extends AbstractResultController<RestResult> {

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
