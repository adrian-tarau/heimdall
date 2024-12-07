package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.heimdall.rest.api.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemRestScenarioController")
@DataSet(model = RestScenario.class, timeFilter = false)
@RequestMapping("/system/rest/scenario")
@Help("rest/system/scenario")
public class RestScenarioController extends DataSetController<RestScenario, Integer> {

    @Autowired
    private RestService restService;

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<RestScenario, Field<RestScenario>, Integer> dataSet, RestScenario model, State state) {
        super.afterPersist(dataSet, model, state);
        restService.reload();
    }
}
