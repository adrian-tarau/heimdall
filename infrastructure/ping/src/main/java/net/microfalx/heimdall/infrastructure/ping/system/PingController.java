package net.microfalx.heimdall.infrastructure.ping.system;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import net.microfalx.heimdall.infrastructure.ping.PingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/system/infrastructure/ping")
@DataSet(model = Ping.class, timeFilter = false)
@Help("infrastructure/ping")
public class PingController extends SystemDataSetController<Ping, Integer> {

    private final PingService pingService;

    public PingController(DataSetService dataSetService, PingService pingService) {
        super(dataSetService);
        this.pingService = pingService;
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<Ping, Field<Ping>, Integer> dataSet, Ping model, State state) {
        super.afterPersist(dataSet, model, state);
        pingService.reload();
    }
}
