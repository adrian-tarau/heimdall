package net.microfalx.heimdall.database.core;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("heimdallDatabaseSessionController")
@RequestMapping("/database/session")
public class SessionController extends net.microfalx.bootstrap.web.controller.admin.database.SessionController {
}
