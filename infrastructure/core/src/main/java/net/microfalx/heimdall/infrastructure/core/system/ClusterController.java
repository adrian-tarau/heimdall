package net.microfalx.heimdall.infrastructure.core.system;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemClusterController")
@RequestMapping("/system/infrastructure/cluster")
@DataSet(model = Cluster.class, timeFilter = false)
@Help("admin/infrastructure/cluster")
public class ClusterController extends SystemDataSetController<Cluster, Integer> {

    @Autowired
    private InfrastructureService infrastructureService;

    @Override
    protected boolean beforePersist(net.microfalx.bootstrap.dataset.DataSet<Cluster, Field<Cluster>, Integer> dataSet, Cluster model, State state) {
        model.setNaturalId(StringUtils.toIdentifier(model.getName()));
        return super.beforePersist(dataSet, model, state);
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<Cluster, Field<Cluster>, Integer> dataSet, Cluster model, State state) {
        super.afterPersist(dataSet, model, state);
        infrastructureService.reload();
    }
}
