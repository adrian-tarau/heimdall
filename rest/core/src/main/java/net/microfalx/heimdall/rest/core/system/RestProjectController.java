package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.dataset.DataSetException;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.component.Button;
import net.microfalx.bootstrap.web.component.Toolbar;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.bootstrap.web.util.JsonFormResponse;
import net.microfalx.bootstrap.web.util.JsonResponse;
import net.microfalx.heimdall.rest.api.Project;
import net.microfalx.heimdall.rest.api.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static net.microfalx.lang.StringUtils.isEmpty;
import static net.microfalx.lang.StringUtils.isNotEmpty;
import static net.microfalx.lang.UriUtils.parseUri;

@Controller("SystemProjectController")
@DataSet(model = RestProject.class, timeFilter = false)
@RequestMapping("/system/rest/project")
@Help("rest/system/project")
public class RestProjectController extends DataSetController<RestProject, Integer> {

    @Autowired
    private RestService restService;

    @Autowired
    private AsyncTaskExecutor executor;

    @PostMapping("sync")
    @ResponseBody
    public JsonResponse<?> sync() {
        restService.reload();
        return JsonResponse.success("The projects synchronization was scheduled to be executed");
    }

    @Override
    protected boolean beforeEdit(net.microfalx.bootstrap.dataset.DataSet<RestProject, Field<RestProject>, Integer> dataSet, Model controllerModel, RestProject dataSetModel) {
        restrictPrivateProjects(dataSetModel);
        return super.beforeEdit(dataSet, controllerModel, dataSetModel);
    }

    @Override
    protected boolean beforeDelete(net.microfalx.bootstrap.dataset.DataSet<RestProject, Field<RestProject>, Integer> dataSet, Model controllerModel, RestProject dataSetModel) {
        restrictPrivateProjects(dataSetModel);
        return super.beforeDelete(dataSet, controllerModel, dataSetModel);
    }

    @Override
    protected void updateToolbar(Toolbar toolbar) {
        super.updateToolbar(toolbar);
        toolbar.add(new Button().setAction("rest.project.sync").setText("Synchronize").setIcon("fa-solid fa-rotate")
                .setDescription("Synchronizes the projects (libraries and simulations) from their code repositories"));
    }

    @Override
    protected void validate(RestProject model, State state, JsonFormResponse<?> response) {
        super.validate(model, state, response);
        if (isNotEmpty(model.getToken()) && isEmpty(model.getUserName())) {
            response.addError("userName", "User name is required when token is present");
        }
        if (isNotEmpty(model.getPassword()) && isEmpty(model.getUserName())) {
            response.addError("password", "User name is required when password is present");
        }
        if (isNotEmpty(model.getUserName()) && (isEmpty(model.getPassword()) && isEmpty(model.getToken()))) {
            response.addError("token", "Token or password is required when user name is present");
            response.addError("password", "Token or password is required when user name is present");
        }
    }

    @Override
    protected boolean beforePersist(net.microfalx.bootstrap.dataset.DataSet<RestProject, Field<RestProject>, Integer> dataSet, RestProject model, State state) {
        model.setNaturalId(Project.getNaturalId(parseUri(model.getUri())));
        return super.beforePersist(dataSet, model, state);
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<RestProject, Field<RestProject>, Integer> dataSet, RestProject model, State state) {
        super.afterPersist(dataSet, model, state);
        Project project = restService.getProject(model.getNaturalId());
        executor.execute(() -> restService.reload(project));
    }

    private void restrictPrivateProjects(RestProject dataSetModel) {
        if (Project.DEFAULT.getId().equals(dataSetModel.getNaturalId())) {
            throw new DataSetException("The default project cannot be removed");
        }
        if (Project.GLOBAL.getId().equals(dataSetModel.getNaturalId())) {
            throw new DataSetException("The global project cannot be removed");
        }
    }
}
