package net.microfalx.heimdall.broker.core;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/system/broker/topic")
@DataSet(model = BrokerTopic.class)
@Help("system/broker/topic")
public class BrokerTopicController extends DataSetController<Broker, Integer> {

    @Autowired
    private BrokerTopicRepository topicRepository;
}
