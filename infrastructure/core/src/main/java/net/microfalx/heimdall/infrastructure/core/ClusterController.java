package net.microfalx.heimdall.infrastructure.core;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/system/infrastructure/cluster")
@DataSet(model = Cluster.class)
@Help("infrastructure/cluster")
public class ClusterController extends DataSetController<Cluster, Integer> {
}
