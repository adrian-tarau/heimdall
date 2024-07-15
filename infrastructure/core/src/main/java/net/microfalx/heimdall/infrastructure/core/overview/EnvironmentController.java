package net.microfalx.heimdall.infrastructure.core.overview;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("OverviewEnvironmentController")
@RequestMapping("/infrastructure/environment")
@DataSet(model = Environment.class, timeFilter = false)
@Help("infrastructure/environment")
public class EnvironmentController extends DataSetController<Environment, String> {
}
