package net.microfalx.heimdall.infrastructure.core.overview;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("OverviewServiceController")
@RequestMapping("/infrastructure/service")
@DataSet(model = Service.class, timeFilter = false, defaultQuery = "active = true")
@Help("infrastructure/service")
public class ServiceController extends DataSetController<Service, String> {

    public ServiceController(DataSetService dataSetService) {
        super(dataSetService);
    }
}
