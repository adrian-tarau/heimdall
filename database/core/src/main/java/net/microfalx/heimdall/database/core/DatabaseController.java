package net.microfalx.heimdall.database.core;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.controller.admin.database.Database;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("heimdallDatabaseController")
@RequestMapping("database")
@DataSet(model = Database.class)
@Help("database")
public class DatabaseController extends DataSetController<Database, String> {
}
