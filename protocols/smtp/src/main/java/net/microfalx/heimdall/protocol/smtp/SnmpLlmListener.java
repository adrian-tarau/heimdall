package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.heimdall.llm.api.LlmListener;
import net.microfalx.heimdall.llm.api.LlmService;
import net.microfalx.heimdall.llm.api.Prompt;
import net.microfalx.lang.annotation.Provider;

@Provider
public class SnmpLlmListener implements LlmListener {

    private static final String MODULE = "smtp";

    @Override
    public void onStart(LlmService service) {
        service.registerPrompt(Prompt.create("summary", "Summary").system(true).fromResources(MODULE).build());
    }
}
