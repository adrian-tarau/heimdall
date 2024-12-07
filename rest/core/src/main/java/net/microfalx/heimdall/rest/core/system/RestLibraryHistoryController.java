package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemRestLibraryHistoryController")
@DataSet(model = RestLibraryHistory.class, timeFilter = false, canAdd = false)
@RequestMapping("/system/rest/rest_library_history")
public class RestLibraryHistoryController extends DataSetController<RestLibraryHistory, Integer> {
}
