package net.microfalx.heimdall.infrastructure.core.system;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/system/infrastructure/environment")
@DataSet(model = Environment.class, timeFilter = false)
@Help("admin/infrastructure/environment")
public class EnvironmentController extends DataSetController<Environment, Integer> {

    @Autowired
    private InfrastructureService infrastructureService;

    @Override
    protected boolean beforePersist(net.microfalx.bootstrap.dataset.DataSet<Environment, Field<Environment>, Integer> dataSet, Environment model, State state) {
        model.setNaturalId(StringUtils.toIdentifier(model.getName()));
        return super.beforePersist(dataSet, model, state);
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<Environment, Field<Environment>, Integer> dataSet, Environment model, State state) {
        super.afterPersist(dataSet, model, state);
        infrastructureService.reload();
    }
}
