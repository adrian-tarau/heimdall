package net.microfalx.heimdall.rest.core.overview;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.heimdall.rest.core.common.AbstractScheduleController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("OverviewScheduleController")
@DataSet(model = Schedule.class, timeFilter = false)
@RequestMapping("/rest/schedule")
@Help("rest/system/schedule")
@Slf4j
public class ScheduleController extends AbstractScheduleController<Schedule> {
}
