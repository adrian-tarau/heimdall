package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/infrastructure/ping")
@DataSet(model = PingResult.class, timeFilter = false)
@Help("infrastructure/ping_result")
public class PingResultController extends DataSetController<PingResult, Integer> {
}
