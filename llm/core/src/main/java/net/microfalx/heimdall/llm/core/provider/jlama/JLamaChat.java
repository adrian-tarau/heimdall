package net.microfalx.heimdall.llm.core.provider.jlama;

import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Prompt;
import net.microfalx.heimdall.llm.core.AbstractChat;

public class JLamaChat extends AbstractChat {

    public JLamaChat(Prompt prompt, Model model) {
        super(prompt, model);
    }


}
