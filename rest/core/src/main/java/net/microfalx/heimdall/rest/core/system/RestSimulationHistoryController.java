package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemRestSimulationHistoryController")
@DataSet(model = RestSimulationHistory.class, timeFilter = false, canAdd = false)
@RequestMapping("/system/rest/rest_simulation_history")
public class RestSimulationHistoryController extends DataSetController<RestSimulationHistory, Integer> {
}
