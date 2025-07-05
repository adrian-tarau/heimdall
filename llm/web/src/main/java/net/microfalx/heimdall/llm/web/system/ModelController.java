package net.microfalx.heimdall.llm.web.system;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import net.microfalx.heimdall.llm.api.LlmService;
import net.microfalx.heimdall.llm.web.system.jpa.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import static net.microfalx.lang.StringUtils.toIdentifier;

@Controller("SystemModelController")
@RequestMapping("/system/ai/model")
@DataSet(model = Model.class, timeFilter = false,canAdd = false,canDelete = false)
public class ModelController extends SystemDataSetController<Model,Integer> {

    @Autowired
    private LlmService llmService;

    @Override
    protected void beforePersist(net.microfalx.bootstrap.dataset.DataSet<Model, Field<Model>, Integer> dataSet, Model model, State state) {
        if (model.getNaturalId() == null) model.setNaturalId(toIdentifier(model.getName()));
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<Model, Field<Model>, Integer> dataSet, Model model, State state) {
        super.afterPersist(dataSet, model, state);
        llmService.reload();
    }
}
