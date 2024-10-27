package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.heimdall.rest.api.Project;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;

@Controller("SystemProjectController")
@DataSet(model = RestProject.class, timeFilter = false)
@RequestMapping("/system/rest/project")
public class RestProjectController extends DataSetController<RestProject, Integer> {

    @Override
    protected boolean beforePersist(net.microfalx.bootstrap.dataset.DataSet<RestProject, Field<RestProject>, Integer> dataSet, RestProject model, State state) {
        model.setNaturalId(Project.getNaturalId(URI.create(model.getUri())));
        return super.beforePersist(dataSet, model, state);
    }
}
