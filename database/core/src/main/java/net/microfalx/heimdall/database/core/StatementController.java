package net.microfalx.heimdall.database.core;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.net.URI;

@Controller
@RequestMapping(value = "/database/statement")
@DataSet(model = Statement.class, viewTemplate = "database/statement_view", viewClasses = "modal-lg")
@Help("database/statement")
public class StatementController extends DataSetController<Statement, Integer> {

    @Autowired
    private StatementRepository statementRepository;

    @Override
    protected void beforeView(net.microfalx.bootstrap.dataset.DataSet<Statement, Field<Statement>, Integer> dataSet, Model controllerModel, Statement dataSetModel) {
        super.beforeView(dataSet, controllerModel, dataSetModel);
        loadContent(dataSetModel, Integer.MAX_VALUE);
    }

    @Override
    protected void beforeBrowse(net.microfalx.bootstrap.dataset.DataSet<Statement, Field<Statement>, Integer> dataSet, Model controllerModel, Statement dataSetModel) {
        super.beforeBrowse(dataSet, controllerModel, dataSetModel);
        if (dataSetModel != null) loadContent(dataSetModel, 70);
    }

    private void loadContent(Statement dataSetModel, int limit) {
        Resource resource = ResourceFactory.resolve(URI.create(dataSetModel.getResource()));
        try {
            if (resource.exists()) {
                dataSetModel.setContent(StringUtils.abbreviate(resource.loadAsString(), limit));
            }
        } catch (IOException e) {
            dataSetModel.setContent(net.microfalx.lang.StringUtils.NA_STRING);
        }
    }
}
