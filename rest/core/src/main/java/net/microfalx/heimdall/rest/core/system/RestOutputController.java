package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemRestOutputController")
@DataSet(model = RestOutput.class, timeFilter = false)
@RequestMapping("/system/rest/result_output")
@Help("rest/system/output")
public class RestOutputController extends DataSetController<RestOutput,Long> {

    public RestOutputController(DataSetService dataSetService) {
        super(dataSetService);
    }
}
