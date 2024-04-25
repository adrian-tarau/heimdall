package net.microfalx.heimdall.broker.core;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/system/broker/topic")
@DataSet(model = BrokerTopic.class)
@Help("system/broker/topic")
public class BrokerTopicController extends DataSetController<BrokerTopic, Integer> {

    @Autowired
    private BrokerTopicRepository topicRepository;

    @Autowired
    private BrokerService brokerService;

    @Override
    protected void updateFields(net.microfalx.bootstrap.dataset.DataSet<BrokerTopic, Field<BrokerTopic>, Integer> dataSet, BrokerTopic model, State state) {
        super.updateFields(dataSet, model, state);
        model.setType(model.getBroker().getType());
    }

    @Override
    protected void beforeBrowse(net.microfalx.bootstrap.dataset.DataSet<BrokerTopic, Field<BrokerTopic>, Integer> dataSet, Model controllerModel, BrokerTopic dataSetModel) {
        super.beforeBrowse(dataSet, controllerModel, dataSetModel);
        if (dataSetModel != null) brokerService.updateStatus(dataSetModel);
    }
}
