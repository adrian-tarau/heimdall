package net.microfalx.heimdall.database.core;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
}
