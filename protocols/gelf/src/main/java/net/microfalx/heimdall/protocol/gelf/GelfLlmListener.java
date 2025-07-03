package net.microfalx.heimdall.protocol.gelf;

import net.microfalx.heimdall.llm.api.LlmListener;
import net.microfalx.heimdall.llm.api.LlmService;
import net.microfalx.heimdall.llm.api.Prompt;
import net.microfalx.lang.annotation.Provider;

@Provider
public class GelfLlmListener implements LlmListener {

    private static final String MODULE = "gelf";

    @Override
    public void onStart(LlmService service) {
        service.registerPrompt(Prompt.create("summary", "Summary").system(true).fromResources(MODULE).build());
        service.registerPrompt(Prompt.create("rca", "Root Cause Analysis").system(true).fromResources(MODULE).build());
    }
}
