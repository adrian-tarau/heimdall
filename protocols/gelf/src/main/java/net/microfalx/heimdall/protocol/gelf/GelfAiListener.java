package net.microfalx.heimdall.protocol.gelf;

import net.microfalx.bootstrap.ai.api.AiListener;
import net.microfalx.bootstrap.ai.api.AiService;
import net.microfalx.bootstrap.ai.api.Prompt;
import net.microfalx.lang.annotation.Provider;

@Provider
public class GelfAiListener implements AiListener {

    private static final String MODULE = "gelf";

    @Override
    public void onStart(AiService service) {
        service.registerPrompt(Prompt.create("summary", "Summary").system(true).fromResources(MODULE).build());
        //service.registerPrompt(Prompt.create("rca", "Root Cause Analysis").system(true).fromResources(MODULE).build());
    }
}
