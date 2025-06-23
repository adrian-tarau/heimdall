package net.microfalx.heimdall.llm.web.system;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.web.component.Button;
import net.microfalx.bootstrap.web.component.Toolbar;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import net.microfalx.heimdall.llm.web.system.jpa.Chat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemChatController")
@RequestMapping("/system/ai/chat")
@DataSet(model = Chat.class)
public class ChatController extends SystemDataSetController<Chat,Integer> {

    @Override
    protected void updateToolbar(Toolbar toolbar) {
        super.updateToolbar(toolbar);
        toolbar.add(new Button().setAction("chat.acknowledge").setText("Acknowledge").setIcon("fa-solid fa-thumbs-up")
                .setDescription("Acknowledges all pending alerts"));
    }
}
