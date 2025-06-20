package net.microfalx.heimdall.protocol.gelf;

import net.microfalx.heimdall.llm.api.LlmListener;
import net.microfalx.heimdall.llm.api.LlmService;
import net.microfalx.heimdall.llm.api.Prompt;
import net.microfalx.lang.annotation.Provider;

@Provider
public class GelfLlmListener implements LlmListener {

    @Override
    public void onStart(LlmService service) {
        service.registerPrompt((Prompt) Prompt.create("gelf.summary", "Summary")
                .tag("gelf").build());
    }
}
