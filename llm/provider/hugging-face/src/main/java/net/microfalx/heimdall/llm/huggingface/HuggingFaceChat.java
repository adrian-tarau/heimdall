package net.microfalx.heimdall.llm.huggingface;

import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Prompt;
import net.microfalx.heimdall.llm.core.AbstractChat;

public class HuggingFaceChat extends AbstractChat {

    public HuggingFaceChat(Prompt prompt, Model model) {
        super(prompt, model);
    }
}
