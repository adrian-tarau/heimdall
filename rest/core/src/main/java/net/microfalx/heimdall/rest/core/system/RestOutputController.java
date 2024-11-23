package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemRestOutputController")
@DataSet(model = RestOutput.class, timeFilter = false)
@RequestMapping("/system/rest/result_output")
public class RestOutputController extends DataSetController<RestOutput,Long> {
}
