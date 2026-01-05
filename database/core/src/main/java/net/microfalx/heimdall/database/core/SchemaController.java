package net.microfalx.heimdall.database.core;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.jdbc.support.Database;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/system/database/manage")
@DataSet(model = Schema.class, timeFilter = false)
@Help("admin/database/manage")
public class SchemaController extends SystemDataSetController<Schema, Integer> {

    private final DatabaseService databaseService;

    public SchemaController(DataSetService dataSetService, DatabaseService databaseService) {
        super(dataSetService);
        this.databaseService = databaseService;
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<Schema, Field<Schema>, Integer> dataSet, Schema model, State state) {
        super.afterPersist(dataSet, model, state);
        databaseService.getThreadPool().execute(() -> databaseService.reload(model));
    }

    @Override
    protected void beforeBrowse(net.microfalx.bootstrap.dataset.DataSet<Schema, Field<Schema>, Integer> dataSet, Model controllerModel, Schema dataSetModel) {
        super.beforeBrowse(dataSet, controllerModel, dataSetModel);
        if (dataSetModel != null) {
            Database database = databaseService.findDatabase(dataSetModel);
            if (database != null) {
                dataSetModel.setState(database.getState());
                dataSetModel.setValidationError(database.getValidationError());
            }
        }
    }
}
