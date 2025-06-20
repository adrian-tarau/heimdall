package net.microfalx.heimdall.llm.web.system;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import net.microfalx.heimdall.llm.web.system.jpa.Chat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemChatController")
@RequestMapping("/system/ai/chat")
@DataSet(model = Chat.class)
public class ChatController extends SystemDataSetController<Chat,Integer> {

}
