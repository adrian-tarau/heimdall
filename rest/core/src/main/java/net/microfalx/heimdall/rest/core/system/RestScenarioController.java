package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemRestScenarioController")
@DataSet(model = RestScenario.class, timeFilter = false)
@RequestMapping("/system/rest/scenario")
@Help("rest/system/scenario")
public class RestScenarioController extends DataSetController<RestScenario, Integer> {
}
