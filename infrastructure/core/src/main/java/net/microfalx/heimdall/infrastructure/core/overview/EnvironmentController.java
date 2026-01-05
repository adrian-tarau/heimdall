package net.microfalx.heimdall.infrastructure.core.overview;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("OverviewEnvironmentController")
@RequestMapping("/infrastructure/environment")
@DataSet(model = Environment.class, timeFilter = false)
@Help("infrastructure/environment")
public class EnvironmentController extends DataSetController<Environment, String> {

    private final InfrastructureService infrastructureService;

    public EnvironmentController(DataSetService dataSetService, InfrastructureService infrastructureService) {
        super(dataSetService);
        this.infrastructureService = infrastructureService;
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<Environment, Field<Environment>, String> dataSet, Environment model, State state) {
        super.afterPersist(dataSet, model, state);
        infrastructureService.reload();
    }
}
