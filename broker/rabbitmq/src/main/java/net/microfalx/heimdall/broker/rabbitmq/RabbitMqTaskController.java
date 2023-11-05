package net.microfalx.heimdall.broker.rabbitmq;

import net.microfalx.heimdall.broker.core.AbstractTaskController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("broker/rabbitmq")
public class RabbitMqTaskController extends AbstractTaskController {
}
