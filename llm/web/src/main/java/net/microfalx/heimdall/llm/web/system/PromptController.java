package net.microfalx.heimdall.llm.web.system;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.HelpService;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import net.microfalx.heimdall.llm.api.LlmService;
import net.microfalx.heimdall.llm.web.system.jpa.Prompt;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller("SystemPromptController")
@RequestMapping("/system/ai/prompt")
@DataSet(model = Prompt.class, timeFilter = false, viewTemplate = "llm/prompt_view", viewClasses = "modal-lg")
@Slf4j
public class PromptController extends SystemDataSetController<Prompt, Integer> {

    @Autowired private LlmService llmService;
    @Autowired private HelpService helpService;

    @Override
    protected void updateModel(net.microfalx.bootstrap.dataset.DataSet<Prompt, Field<Prompt>, Integer> dataSet, Model controllerModel, Prompt dataSetModel, State state) {
        if (state != State.VIEW) return;
        /*dataSetModel.setExamples(renderMarkdown(dataSetModel.getExamples()));
        dataSetModel.setRole(renderMarkdown(dataSetModel.getRole()));
        dataSetModel.setContext(renderMarkdown(dataSetModel.getContext()));
        dataSetModel.setQuestion(renderMarkdown(dataSetModel.getQuestion()));*/
    }

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

    private String renderMarkdown(String text) {
        return renderMarkdown(Resource.text(text));
    }

    private String renderMarkdown(Resource resource) {
        try {
            return helpService.render(resource);
        } catch (IOException e) {
            LOGGER.atError().setCause(e).log("Failed to render markdown: {}", resource.toURI());
            try {
                return resource.loadAsString();
            } catch (IOException ex) {
                return "#Error: content not available";
            }
        }
    }
}
