package net.microfalx.heimdall.llm.core.provider.onnx;

import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Prompt;
import net.microfalx.heimdall.llm.core.AbstractChat;

public class OnnxChat extends AbstractChat {

    public OnnxChat(Prompt prompt, Model model) {
        super(prompt, model);
    }
}
