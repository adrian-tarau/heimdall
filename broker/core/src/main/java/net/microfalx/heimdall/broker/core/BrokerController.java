package net.microfalx.heimdall.broker.core;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/system/broker/manage")
@DataSet(model = Broker.class)
@Help("system/broker/manage")
public class BrokerController extends DataSetController<Broker, Integer> {

    @Autowired
    private BrokerRepository brokerRepository;

    @Autowired
    private BrokerService brokerService;

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<Broker, Field<Broker>, Integer> dataSet, Broker model, State state) {
        super.afterPersist(dataSet, model, state);
        brokerService.reload();
    }

    @Override
    protected void beforeBrowse(net.microfalx.bootstrap.dataset.DataSet<Broker, Field<Broker>, Integer> dataSet, Model controllerModel, Broker dataSetModel) {
        super.beforeBrowse(dataSet, controllerModel, dataSetModel);
        if (dataSetModel != null) {
            dataSetModel.setParameters(StringUtils.abbreviate(dataSetModel.getParameters(), 70));
        }
    }
}
