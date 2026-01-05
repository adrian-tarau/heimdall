package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.heimdall.rest.api.RestService;
import net.microfalx.heimdall.rest.core.common.AbstractLibraryHistoryController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemRestLibraryHistoryController")
@DataSet(model = RestLibraryHistory.class, timeFilter = false, canAdd = false)
@RequestMapping("/system/rest/library/history")
public class RestLibraryHistoryController extends AbstractLibraryHistoryController<RestLibraryHistory, RestLibrary> {

    private final RestLibraryRepository restLibraryRepository;
    private final RestLibraryHistoryRepository restLibraryHistoryRepository;

    public RestLibraryHistoryController(DataSetService dataSetService, RestService restService, ContentService contentService, RestLibraryRepository restLibraryRepository, RestLibraryHistoryRepository restLibraryHistoryRepository) {
        super(dataSetService, restService, contentService);
        this.restLibraryRepository = restLibraryRepository;
        this.restLibraryHistoryRepository = restLibraryHistoryRepository;
    }

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
