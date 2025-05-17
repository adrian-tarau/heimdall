package net.microfalx.heimdall.llm.ollama;

import net.microfalx.heimdall.llm.api.Chat;
import net.microfalx.heimdall.llm.api.ChatFactory;
import net.microfalx.heimdall.llm.api.Model;

public class OllamaChatFactory implements ChatFactory {

    @Override
    public Chat createChat(String id, Model model) {
        return null;
    }

    @Override
    public Chat createChat(String id, String name, Model model) {
        return null;
    }
}
