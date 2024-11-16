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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import static net.microfalx.lang.StringUtils.defaultIfEmpty;
import static net.microfalx.lang.UriUtils.SLASH;

@Controller
@RequestMapping("/system/infrastructure/environment")
@DataSet(model = Environment.class, timeFilter = false)
@Help("admin/infrastructure/environment")
public class EnvironmentController extends SystemDataSetController<Environment, Integer> {

    @Autowired
    private InfrastructureService infrastructureService;

    @Override
    protected void beforeBrowse(net.microfalx.bootstrap.dataset.DataSet<Environment, Field<Environment>, Integer> dataSet, Model controllerModel, Environment dataSetModel) {
        super.beforeBrowse(dataSet, controllerModel, dataSetModel);
        if (dataSetModel != null) {
            try {
                dataSetModel.setClusterCount(dataSetModel.getClusters().size());
            } catch (Exception e) {
                dataSetModel.setClusterCount(-1);
            }
            try {
                dataSetModel.setServerCount(dataSetModel.getServers().size());
            } catch (Exception e) {
                dataSetModel.setServerCount(-1);
            }
        }
    }

    @Override
    protected boolean beforePersist(net.microfalx.bootstrap.dataset.DataSet<Environment, Field<Environment>, Integer> dataSet, Environment model, State state) {
        model.setNaturalId(StringUtils.toIdentifier(model.getName()));
        model.setApiPath(defaultIfEmpty(model.getApiPath(), SLASH));
        model.setAppPath(defaultIfEmpty(model.getApiPath(), SLASH));
        return super.beforePersist(dataSet, model, state);
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<Environment, Field<Environment>, Integer> dataSet, Environment model, State state) {
        super.afterPersist(dataSet, model, state);
        infrastructureService.reload();
    }
}
