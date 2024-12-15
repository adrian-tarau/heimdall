package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.heimdall.rest.core.common.AbstractLibraryHistoryController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemRestLibraryHistoryController")
@DataSet(model = RestLibraryHistory.class, timeFilter = false, canAdd = false)
@RequestMapping("/system/rest/library/history")
public class RestLibraryHistoryController extends AbstractLibraryHistoryController<RestLibraryHistory, RestLibrary> {

    @Autowired
    private RestLibraryRepository restLibraryRepository;

    @Autowired
    private RestLibraryHistoryRepository restLibraryHistoryRepository;

    @Override
    protected String getTitle() {
        return "Library";
    }

    @Override
    protected RestLibraryHistory getHistory(int id) {
        return restLibraryHistoryRepository.findById(id).orElse(null);
    }

    @Override
    protected RestLibrary getLibrary(RestLibraryHistory history) {
        return history.getRestLibrary();
    }

    @Override
    protected void save(RestLibrary library) {
        restLibraryRepository.save(library);
    }
}
