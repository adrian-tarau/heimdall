package net.microflax.heimdall.llm.github;

import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Prompt;
import net.microfalx.heimdall.llm.core.AbstractChat;

public class GithubChat extends AbstractChat {

    public GithubChat(Prompt prompt, Model model) {
        super(prompt, model);
    }
}
