package net.microfalx.heimdall.llm.web.chat;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.dataset.DataSet;
import net.microfalx.bootstrap.dataset.DataSetRequest;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.component.Button;
import net.microfalx.bootstrap.web.component.Item;
import net.microfalx.bootstrap.web.component.Menu;
import net.microfalx.bootstrap.web.component.Toolbar;
import net.microfalx.bootstrap.web.dataset.DataSetControllerListener;
import net.microfalx.heimdall.llm.api.LlmService;
import net.microfalx.heimdall.llm.api.Prompt;
import net.microfalx.lang.annotation.Provider;

import java.util.Collection;

@Provider
public class ChatDataSetControllerListener<M, F extends Field<M>, ID> extends ApplicationContextSupport
        implements DataSetControllerListener<M, F, ID> {

    @Override
    public void updateToolbar(DataSetRequest<M, F, ID> request, Toolbar toolbar) {
        if (!request.getDataSet().getTags().contains("ai")) return;
        Menu menu = getAiMenu(request);
        if (!menu.hasChildren()) return;
        toolbar.add(new Button().setText("Ask AI").setIcon("fa-solid fa-robot").setPosition(1000)
                .setDescription("Ask AI to help answer questions relate to this dashboard")
                .setMenu(menu));
    }

    private Menu getAiMenu(DataSetRequest<M, F, ID> request) {
        DataSet<M, F, ID> dataSet = request.getDataSet();
        LlmService llmService = getBean(LlmService.class);
        Collection<Prompt> prompts = llmService.getPrompts(dataSet.getTags());
        Menu menu = new Menu().setId("ai");
        for (Prompt prompt : prompts) {
            Item item = new Item().setAction("chat.prompt").setText(prompt.getName());
            item.addParameter("prompt", prompt.getId());
            item.addParameter("dataSet", request.getId());
            menu.add(item);
        }
        return menu;
    }
}
