package net.microfalx.heimdall.database.mysql;

import net.microfalx.heimdall.database.core.AbstractConnectionController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("database/mysql")
public class MySqlConnectionController extends AbstractConnectionController {
}
