package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.bootstrap.web.util.JsonFormResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemScheduleController")
@DataSet(model = Schedule.class, timeFilter = false)
@RequestMapping("/system/rest/schedule")
public class ScheduleController extends DataSetController<Schedule,Integer> {

    @Override
    protected void validate(Schedule model, State state, JsonFormResponse<?> response) {
        super.validate(model, state, response);
    }
}
