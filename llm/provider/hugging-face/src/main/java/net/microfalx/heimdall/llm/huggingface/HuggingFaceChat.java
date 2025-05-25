package net.microfalx.heimdall.llm.huggingface;

import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.core.AbstractChat;

public class HuggingFaceChat extends AbstractChat {
    public HuggingFaceChat(Model model) {
        super(model);
    }
}
