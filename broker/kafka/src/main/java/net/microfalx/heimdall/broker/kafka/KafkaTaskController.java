package net.microfalx.heimdall.broker.kafka;

import net.microfalx.heimdall.broker.core.AbstractTaskController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/broker/kafka")
public class KafkaTaskController extends AbstractTaskController {
}
