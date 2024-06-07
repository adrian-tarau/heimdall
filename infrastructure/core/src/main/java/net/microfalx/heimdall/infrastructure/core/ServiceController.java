package net.microfalx.heimdall.infrastructure.core;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/system/infrastructure/service")
@DataSet(model = Service.class, timeFilter = false)
@Help("infrastructure/service")
public class ServiceController extends DataSetController<Service, Integer> {

    @Override
    protected boolean beforePersist(net.microfalx.bootstrap.dataset.DataSet<Service, Field<Service>, Integer> dataSet, Service model, State state) {
        if (model.getNaturalId() == null) model.setNaturalId(Service.toNaturalId(model));
        return super.beforePersist(dataSet, model, state);
    }
}
