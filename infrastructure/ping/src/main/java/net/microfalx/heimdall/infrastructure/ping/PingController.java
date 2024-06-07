package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/system/infrastructure/ping")
@DataSet(model = Ping.class, timeFilter = false)
@Help("infrastructure/ping")
public class PingController extends DataSetController<Ping,Integer> {
}
