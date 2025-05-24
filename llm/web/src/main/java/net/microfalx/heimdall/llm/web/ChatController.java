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

@Controller("SystemChatController")
@RequestMapping("/system/ai/chat")
@DataSet(model = Chat.class, timeFilter = false)
public class ChatController extends SystemDataSetController<Chat,Integer> {

    @Autowired
    private LlmService llmService;

    @Override
    protected boolean beforePersist(net.microfalx.bootstrap.dataset.DataSet<Chat, Field<Chat>, Integer> dataSet, Chat model, State state) {
        model.setNaturalId(StringUtils.toIdentifier(model.getName()));
        return super.beforePersist(dataSet, model, state);
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<Chat, Field<Chat>, Integer> dataSet, Chat model, State state) {
        super.afterPersist(dataSet, model, state);
        llmService.reload();
    }
}
