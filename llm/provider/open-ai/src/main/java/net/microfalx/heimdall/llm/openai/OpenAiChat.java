package net.microfalx.heimdall.llm.openai;

import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Prompt;
import net.microfalx.heimdall.llm.core.AbstractChat;

public class OpenAiChat extends AbstractChat {

    public OpenAiChat(Prompt prompt, Model model) {
        super(prompt, model);
    }
}
