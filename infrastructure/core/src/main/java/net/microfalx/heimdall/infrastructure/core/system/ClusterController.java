package net.microfalx.heimdall.infrastructure.core.system;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemClusterController")
@RequestMapping("/system/infrastructure/cluster")
@DataSet(model = Cluster.class, timeFilter = false)
@Help("admin/infrastructure/cluster")
public class ClusterController extends SystemDataSetController<Cluster, Integer> {

    private final InfrastructureService infrastructureService;

    public ClusterController(DataSetService dataSetService, InfrastructureService infrastructureService) {
        super(dataSetService);
        this.infrastructureService = infrastructureService;
    }

    @Override
    protected void beforePersist(net.microfalx.bootstrap.dataset.DataSet<Cluster, Field<Cluster>, Integer> dataSet, Cluster model, State state) {
        if (model.getNaturalId() == null) model.setNaturalId(StringUtils.toIdentifier(model.getName()));
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<Cluster, Field<Cluster>, Integer> dataSet, Cluster model, State state) {
        super.afterPersist(dataSet, model, state);
        infrastructureService.reload();
    }
}
