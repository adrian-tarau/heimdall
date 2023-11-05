package net.microfalx.heimdall.broker.pulsar;

import net.microfalx.heimdall.broker.core.AbstractTaskController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/broker/pulsar")
public class PulsarTaskController extends AbstractTaskController {
}
