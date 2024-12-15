package net.microfalx.heimdall.rest.core.common;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.component.Item;
import net.microfalx.bootstrap.web.component.Menu;
import net.microfalx.bootstrap.web.component.Separator;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.bootstrap.web.util.JsonFormResponse;
import net.microfalx.bootstrap.web.util.JsonResponse;
import net.microfalx.heimdall.rest.api.RestService;
import net.microfalx.heimdall.rest.api.Schedule;
import net.microfalx.heimdall.rest.api.SimulationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static net.microfalx.lang.JvmUtils.STARTUP_TIME;
import static net.microfalx.lang.StringUtils.formatMessage;
import static net.microfalx.lang.StringUtils.isEmpty;
import static net.microfalx.lang.TimeUtils.parseDuration;

@Slf4j
public abstract class AbstractScheduleController<T extends AbstractSchedule> extends DataSetController<T, Integer> {

    @Autowired
    protected RestService restService;

    @Autowired
    private TaskExecutor executor;

    @PostMapping("run/{id}")
    @ResponseBody
    public JsonResponse<?> schedule(@PathVariable("id") String id) {
        Schedule schedule = restService.getSchedule(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SimulationContext context = restService.createContext(schedule.getEnvironment(), schedule.getSimulation())
                .setManual(true);
        if (authentication != null) context.setUser(authentication.getName());
        context.getAttributes().copyFrom(schedule.getAttributes(true));
        restService.schedule(context);
        return JsonResponse.success(formatMessage("The simulation ''{0}'' was scheduled to be executed using environment ''{1}''",
                schedule.getSimulation().getName(), schedule.getEnvironment().getName()));
    }

    @Override
    protected void updateActions(Menu menu) {
        super.updateActions(menu);
        if (!getDataSet().isReadOnly()) menu.add(new Separator());
        menu.add(new Item().setAction("rest.schedule.run").setText("Run").setIcon("fa-solid fa-play").setDescription("Runs the simulation"));
    }

    @Override
    protected void validate(T model, State state, JsonFormResponse<?> response) {
        super.validate(model, state, response);
        createTrigger(model, response);
    }

    @Override
    protected void beforeBrowse(net.microfalx.bootstrap.dataset.DataSet<T, Field<T>, Integer> dataSet, Model controllerModel, T dataSetModel) {
        super.beforeBrowse(dataSet, controllerModel, dataSetModel);
        if (dataSetModel != null) {
            try {
                dataSetModel.setNextRunAt(getNextRun(dataSetModel));
            } catch (Exception e) {
                LOGGER.warn("Failed to calculate next run for '{}'", dataSetModel.getId(), e);
            }
        }
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<T, Field<T>, Integer> dataSet, T model, State state) {
        super.afterPersist(dataSet, model, state);
        restService.reload();
        Schedule schedule = restService.getSchedule(Integer.toString(model.getId()));
        executor.execute(() -> restService.reload(schedule));
    }

    private Trigger createTrigger(T model, JsonFormResponse<?> response) {
        switch (model.getType()) {
            case EXPRESSION:
                if (isEmpty(model.getExpression())) {
                    response.addError("expression", "An expression is required");
                    return null;
                } else {
                    try {
                        return new CronTrigger(model.getExpression());
                    } catch (Exception e) {
                        response.addError("expression", "The cron expression is invalid");
                    }
                }
                break;
            case INTERVAL:
                if (isEmpty(model.getInterval())) {
                    response.addError("interval", "An interval is required");
                    return null;
                } else {
                    return new PeriodicTrigger(parseDuration(model.getInterval()));
                }
        }
        return null;
    }

    private LocalDateTime getNextRun(T model) {
        Schedule schedule = restService.getSchedule(Integer.toString(model.getId()));
        JsonFormResponse<?> response = JsonFormResponse.success();
        Trigger trigger = createTrigger(model, response);
        ZonedDateTime lastRun = restService.getLastRun(schedule.getSimulation(), schedule.getEnvironment()).orElse(STARTUP_TIME)
                .atZone(ZoneId.systemDefault());
        switch (schedule.getType()) {
            case EXPRESSION:
                CronTrigger cronTrigger = (CronTrigger) trigger;
                SimpleTriggerContext triggerContext = new SimpleTriggerContext();
                triggerContext.update(lastRun.toInstant(), lastRun.toInstant(), null);
                return cronTrigger.nextExecution(triggerContext).atZone(ZoneId.systemDefault()).toLocalDateTime();
            case INTERVAL:
                return lastRun.plus(schedule.getInterval()).toLocalDateTime();
            default:
                throw new IllegalStateException("Unexpected value: " + schedule.getType());
        }
    }
}
