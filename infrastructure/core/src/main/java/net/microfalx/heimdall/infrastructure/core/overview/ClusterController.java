package net.microfalx.heimdall.infrastructure.core.overview;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.heimdall.infrastructure.core.system.Cluster;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("OverviewClusterController")
@RequestMapping("/infrastructure/cluster")
@DataSet(model = Cluster.class, timeFilter = false)
@Help("infrastructure/cluster")
public class ClusterController extends DataSetController<Cluster, String> {
}
