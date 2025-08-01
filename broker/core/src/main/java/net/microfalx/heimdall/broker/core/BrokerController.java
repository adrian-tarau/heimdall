package net.microfalx.heimdall.broker.core;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

@Controller
@RequestMapping("/system/broker/manage")
@DataSet(model = Broker.class, timeFilter = false)
@Help("system/broker/manage")
public class BrokerController extends SystemDataSetController<Broker, Integer> {

    @Autowired
    private BrokerRepository brokerRepository;

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private TaskExecutor taskExecutor;

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<Broker, Field<Broker>, Integer> dataSet, Broker model, State state) {
        super.afterPersist(dataSet, model, state);
        taskExecutor.execute(() -> brokerService.reload(model));
    }

    @Override
    protected void beforeBrowse(net.microfalx.bootstrap.dataset.DataSet<Broker, Field<Broker>, Integer> dataSet, Model controllerModel, Broker dataSetModel) {
        super.beforeBrowse(dataSet, controllerModel, dataSetModel);
        if (dataSetModel != null) {
            dataSetModel.setParameters(dataSetModel.getParameters());
            String endPoints = getEndPoints(dataSetModel);
            dataSetModel.setEndPoints(endPoints);
        }
    }

    private String getEndPoints(Broker model) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(model.getParameters()));
        } catch (IOException e) {
            // ignore, it should not happen ideally
        }
        String propName = switch (model.getType()) {
            case KAFKA -> "bootstrap.servers";
            case PULSAR -> "serviceUrl";
            case RABBITMQ -> "url";
        };
        return (String) properties.get(propName);
    }
}
