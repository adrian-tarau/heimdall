package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemLibraryController")
@DataSet(model = RestLibrary.class, timeFilter = false)
@RequestMapping("/system/rest/library")
public class RestLibraryController extends DataSetController<RestLibrary,Integer> {
}
