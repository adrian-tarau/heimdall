package net.microfalx.heimdall.database.vertica;

import net.microfalx.heimdall.database.core.AbstractConnectionController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("database/vertica")
public class VerticaConnectionController extends AbstractConnectionController {
}
