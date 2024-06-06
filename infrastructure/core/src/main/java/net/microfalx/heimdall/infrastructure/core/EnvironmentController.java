package net.microfalx.heimdall.infrastructure.core;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/system/infrastructure/environment")
@DataSet(model = Environment.class)
@Help("infrastructure/environment")
public class EnvironmentController extends DataSetController<Environment, Integer> {
}
