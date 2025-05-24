package net.microfalx.heimdall.llm.web;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import net.microfalx.heimdall.llm.api.LlmService;
import net.microfalx.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemModelController")
@RequestMapping("/system/ai/model")
@DataSet(model = Model.class, timeFilter = false,canAdd = false,canDelete = false)
public class ModelController extends SystemDataSetController<Model,Integer> {

    @Autowired
    private LlmService llmService;

    @Override
    protected boolean beforePersist(net.microfalx.bootstrap.dataset.DataSet<Model, Field<Model>, Integer> dataSet, Model model, State state) {
        model.setNaturalId(StringUtils.toIdentifier(model.getName()));
        return super.beforePersist(dataSet, model, state);
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<Model, Field<Model>, Integer> dataSet, Model model, State state) {
        super.afterPersist(dataSet, model, state);
        llmService.reload();
    }
}
