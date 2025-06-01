package net.microfalx.heimdall.llm.core.provider;

import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.core.AbstractChat;

public class OnnxChat extends AbstractChat {

    public OnnxChat(Model model) {
        super(model);
    }
}
