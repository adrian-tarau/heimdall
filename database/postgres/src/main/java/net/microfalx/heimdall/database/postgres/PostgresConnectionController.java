package net.microfalx.heimdall.database.postgres;

import net.microfalx.heimdall.database.core.AbstractConnectionController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("database/postgres")
public class PostgresConnectionController extends AbstractConnectionController {
}
