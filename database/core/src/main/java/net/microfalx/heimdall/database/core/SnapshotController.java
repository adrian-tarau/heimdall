package net.microfalx.heimdall.database.core;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/database/snapshot")
@DataSet(model = Snapshot.class)
@Help("database/snapshot")
public class SnapshotController extends DataSetController<Snapshot, Integer> {

    @Autowired
    private SnapshotRepository snapshotRepository;

}
