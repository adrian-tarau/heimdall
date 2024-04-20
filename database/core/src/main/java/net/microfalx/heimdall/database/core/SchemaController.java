package net.microfalx.heimdall.database.core;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.jdbc.support.Database;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/system/database/manage")
@DataSet(model = Schema.class)
@Help("admin/database/manage")
public class SchemaController extends DataSetController<Schema, Integer> {

    @Autowired
    private SchemaRepository schemaRepository;

    @Autowired
    private DatabaseService databaseService;

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<Schema, Field<Schema>, Integer> dataSet, Schema model, State state) {
        super.afterPersist(dataSet, model, state);
        databaseService.reload();
    }

    @Override
    protected void beforeBrowse(net.microfalx.bootstrap.dataset.DataSet<Schema, Field<Schema>, Integer> dataSet, Model controllerModel, Schema dataSetModel) {
        super.beforeBrowse(dataSet, controllerModel, dataSetModel);
        if (dataSetModel != null) {
            Database database = databaseService.findDatabase(dataSetModel);
            if (database != null) {
                dataSetModel.setState(database.getState());
                dataSetModel.setValidationError(StringUtils.abbreviate(database.getValidationError(), 50));
            }
        }
    }
}
