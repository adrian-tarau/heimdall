package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemRestResultController")
@DataSet(model = RestResult.class, timeFilter = false)
@RequestMapping("/system/rest/result")
public class RestResultController extends DataSetController<RestResult,Long> {
}
