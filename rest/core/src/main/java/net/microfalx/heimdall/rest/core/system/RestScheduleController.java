package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.bootstrap.web.util.JsonFormResponse;
import net.microfalx.heimdall.rest.api.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemScheduleController")
@DataSet(model = RestSchedule.class, timeFilter = false)
@RequestMapping("/system/rest/schedule")
public class RestScheduleController extends DataSetController<RestSchedule,Integer> {

    @Autowired
    private RestService restService;

    @Override
    protected void validate(RestSchedule model, State state, JsonFormResponse<?> response) {
        super.validate(model, state, response);
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<RestSchedule, Field<RestSchedule>, Integer> dataSet, RestSchedule model, State state) {
        super.afterPersist(dataSet, model, state);
        restService.reload();
    }
}
