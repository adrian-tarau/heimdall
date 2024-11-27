package net.microfalx.heimdall.rest.core.system;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.heimdall.rest.core.common.AbstractScheduleController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemScheduleController")
@DataSet(model = RestSchedule.class, timeFilter = false)
@RequestMapping("/system/rest/schedule")
@Help("rest/system/schedule")
@Slf4j
public class RestScheduleController extends AbstractScheduleController<RestSchedule> {
}
