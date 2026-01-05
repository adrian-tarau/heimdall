package net.microfalx.heimdall.infrastructure.core.system;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/system/infrastructure/server")
@DataSet(model = Server.class, timeFilter = false)
@Help("admin/infrastructure/server")
public class ServerController extends SystemDataSetController<Server, Integer> {

    private final InfrastructureService infrastructureService;

    public ServerController(DataSetService dataSetService, InfrastructureService infrastructureService) {
        super(dataSetService);
        this.infrastructureService = infrastructureService;
    }

    @Override
    protected void beforePersist(net.microfalx.bootstrap.dataset.DataSet<Server, Field<Server>, Integer> dataSet, Server model, State state) {
        if (model.getNaturalId() == null) model.setNaturalId(Server.toNaturalId(model));
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<Server, Field<Server>, Integer> dataSet, Server model, State state) {
        super.afterPersist(dataSet, model, state);
        infrastructureService.reload();
    }
}
