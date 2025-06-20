package net.microfalx.heimdall.llm.web.system;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import net.microfalx.heimdall.llm.api.LlmService;
import net.microfalx.heimdall.llm.web.system.jpa.Prompt;
import net.microfalx.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemPromptController")
@RequestMapping("/system/ai/prompt")
@DataSet(model = Prompt.class, timeFilter = false)
public class PromptController extends SystemDataSetController<Prompt, Integer> {

    @Autowired
    private LlmService llmService;

    @Override
    protected boolean beforePersist(net.microfalx.bootstrap.dataset.DataSet<Prompt, Field<Prompt>, Integer> dataSet, Prompt model, State state) {
        model.setNaturalId(StringUtils.toIdentifier(model.getName()));
        return super.beforePersist(dataSet, model, state);
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<Prompt, Field<Prompt>, Integer> dataSet, Prompt model, State state) {
        super.afterPersist(dataSet, model, state);
        llmService.reload();
    }
}
