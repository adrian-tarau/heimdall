package net.microfalx.heimdall.infrastructure.core.overview;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("OverviewServerController")
@RequestMapping("/infrastructure/server")
@DataSet(model = Server.class, timeFilter = false)
@Help("infrastructure/server")
public class ServerController extends DataSetController<Server, String> {

    public ServerController(DataSetService dataSetService) {
        super(dataSetService);
    }
}
