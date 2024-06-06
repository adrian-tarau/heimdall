package net.microfalx.heimdall.infrastructure.core;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/system/infrastructure/service")
@DataSet(model = Service.class)
@Help("infrastructure/service")
public class ServiceController extends DataSetController<Service, Integer> {
}
