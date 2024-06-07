package net.microfalx.heimdall.infrastructure.core;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/system/infrastructure/server")
@DataSet(model = Server.class, timeFilter = false)
@Help("infrastructure/server")
public class ServerController extends DataSetController<Server, Integer> {

    @Override
    protected boolean beforePersist(net.microfalx.bootstrap.dataset.DataSet<Server, Field<Server>, Integer> dataSet, Server model, State state) {
        if (model.getNaturalId() == null) model.setNaturalId(Server.toNaturalId(model));
        return super.beforePersist(dataSet, model, state);
    }
}
