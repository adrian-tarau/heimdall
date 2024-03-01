package net.microfalx.heimdall.database.core;

import net.microfalx.bootstrap.web.controller.UnderConstructionController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("heimdallDatabaseSessionController")
@RequestMapping("/database/connection")
public abstract class SessionController extends UnderConstructionController {
}
