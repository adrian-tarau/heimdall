package net.microfalx.heimdall.llm.huggingface;

import net.microfalx.heimdall.llm.api.Chat;
import net.microfalx.heimdall.llm.api.ChatFactory;
import net.microfalx.heimdall.llm.api.Model;

public class HuggingFaceChatFactory implements ChatFactory {

    @Override
    public Chat createChat(String id, Model model) {
        return null;
    }

    @Override
    public Chat createChat(String id, String name, Model model) {
        return null;
    }
}
