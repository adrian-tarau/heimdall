package net.microfalx.heimdall.llm.web.system;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import net.microfalx.heimdall.llm.api.LlmService;
import net.microfalx.heimdall.llm.web.system.jpa.Provider;
import net.microfalx.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemProviderController")
@RequestMapping("/system/ai/provider")
@DataSet(model = Provider.class, timeFilter = false, canAdd = false, canDelete = false)
public class ProviderController extends SystemDataSetController<Provider, Integer> {

    @Autowired
    private LlmService llmService;

    @Override
    protected boolean beforePersist(net.microfalx.bootstrap.dataset.DataSet<Provider, Field<Provider>, Integer> dataSet, Provider model, State state) {
        model.setNaturalId(StringUtils.toIdentifier(model.getName()));
        return super.beforePersist(dataSet, model, state);
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<Provider, Field<Provider>, Integer> dataSet, Provider model, State state) {
        super.afterPersist(dataSet, model, state);
        llmService.reload();
    }

}
