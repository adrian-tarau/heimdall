package net.microfalx.heimdall.llm.openai;

import net.microfalx.heimdall.llm.api.AiListener;
import net.microfalx.heimdall.llm.api.AiService;
import net.microfalx.lang.annotation.Provider;

@Provider
public class OpenAiListener implements AiListener {

    @Override
    public void registerProviders(AiService service) {

    }
}
