package net.microfalx.heimdall.database.core;

import net.microfalx.bootstrap.dataset.DataSetService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("heimdallDatabaseNodeController")
@RequestMapping("/database/node")
public class NodeController extends net.microfalx.bootstrap.web.controller.admin.database.NodeController {

    public NodeController(DataSetService dataSetService) {
        super(dataSetService);
    }
}
