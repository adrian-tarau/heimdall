package net.microfalx.heimdall.llm.ollama;

import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Prompt;
import net.microfalx.heimdall.llm.core.AbstractChat;

public class OllamaChat extends AbstractChat {

    public OllamaChat(Prompt prompt, Model model) {
        super(prompt,model);
    }
}
