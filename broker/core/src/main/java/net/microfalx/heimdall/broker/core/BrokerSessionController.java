package net.microfalx.heimdall.broker.core;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/broker/session")
@DataSet(model = BrokerSession.class)
@Help("/broker/session")
public class BrokerSessionController extends DataSetController<BrokerSession, Integer> {

    @Autowired
    private BrokerSessionRepository sessionRepository;
}
