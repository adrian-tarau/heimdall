package net.microfalx.heimdall.llm.core;

import net.microfalx.heimdall.llm.api.LlmListener;
import net.microfalx.heimdall.llm.api.LlmService;
import net.microfalx.heimdall.llm.api.Prompt;
import net.microfalx.lang.annotation.Provider;

@Provider
public class DefaultLlmListener implements LlmListener {

    @Override
    public void onStart(LlmService service) {
        registerPrompts(service);
    }

    private void registerPrompts(LlmService service) {
        Prompt prompt = (Prompt) Prompt.create("summary", "Summary")
                .question("Summarize the current data")
                .tag("summary")
                .description("Creates a summary of the current data")
                .build();
        service.registerPrompt(prompt);
    }
}
