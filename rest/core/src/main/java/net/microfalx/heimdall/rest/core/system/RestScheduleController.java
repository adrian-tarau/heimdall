package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.component.Item;
import net.microfalx.bootstrap.web.component.Menu;
import net.microfalx.bootstrap.web.component.Separator;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.bootstrap.web.util.JsonFormResponse;
import net.microfalx.bootstrap.web.util.JsonResponse;
import net.microfalx.heimdall.rest.api.RestService;
import net.microfalx.heimdall.rest.api.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static net.microfalx.lang.StringUtils.isEmpty;

@Controller("SystemScheduleController")
@DataSet(model = RestSchedule.class, timeFilter = false)
@RequestMapping("/system/rest/schedule")
public class RestScheduleController extends DataSetController<RestSchedule,Integer> {

    @Autowired
    private RestService restService;

    @PostMapping("schedule/{id}")
    @ResponseBody
    public JsonResponse<?> schedule(@PathVariable("id") String id) {
        Schedule schedule = restService.getSchedule(id);
        restService.simulate(schedule.getSimulation(), schedule.getEnvironment());
        return JsonResponse.success("The simulation '" + schedule.getName() + "' was scheduled to be executed");
    }

    @Override
    protected void updateActions(Menu menu) {
        super.updateActions(menu);
        menu.add(new Separator());
        menu.add(new Item().setAction("rest.schedule.run").setText("Run").setIcon("fa-solid fa-play").setDescription("Runs the simulation"));
    }

    @Override
    protected void validate(RestSchedule model, State state, JsonFormResponse<?> response) {
        super.validate(model, state, response);
        switch (model.getType()) {
            case EXPRESSION:
                if (isEmpty(model.getExpression())) {
                    response.addError("expression", "An expression is required");
                } else {
                    try {
                        new CronTrigger(model.getExpression());
                    } catch (Exception e) {
                        response.addError("expression", "The cron expression is invalid");
                    }
                }
                break;
            case INTERVAL:
                if (isEmpty(model.getInterval())) {
                    response.addError("interval", "An interval is required");
                }
        }
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<RestSchedule, Field<RestSchedule>, Integer> dataSet, RestSchedule model, State state) {
        super.afterPersist(dataSet, model, state);
        restService.reload();
    }
}
