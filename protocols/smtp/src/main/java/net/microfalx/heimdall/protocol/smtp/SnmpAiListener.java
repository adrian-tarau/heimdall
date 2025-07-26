package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.bootstrap.ai.api.AiListener;
import net.microfalx.bootstrap.ai.api.AiService;
import net.microfalx.bootstrap.ai.api.Prompt;
import net.microfalx.lang.annotation.Provider;

@Provider
public class SnmpAiListener implements AiListener {

    private static final String MODULE = "smtp";

    @Override
    public void onStart(AiService service) {
        service.registerPrompt(Prompt.create("summary", "Summary").system(true).fromResources(MODULE).build());
    }
}
