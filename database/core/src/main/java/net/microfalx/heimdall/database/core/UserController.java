package net.microfalx.heimdall.database.core;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("databaseUserController")
@RequestMapping(value = "/database/user")
@DataSet(model = User.class, timeFilter = false)
@Help("database/user")
public class UserController extends DataSetController<User, Integer> {

    public UserController(DataSetService dataSetService) {
        super(dataSetService);
    }
}
