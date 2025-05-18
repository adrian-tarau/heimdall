package net.microfalx.heimdall.llm.ollama;

import net.microfalx.heimdall.llm.api.AiListener;
import net.microfalx.heimdall.llm.api.AiService;
import net.microfalx.lang.annotation.Provider;

@Provider
public class OllamaListener implements AiListener {

    @Override
    public void registerProviders(AiService service) {
        net.microfalx.heimdall.llm.api.Provider.Builder builder = new net.microfalx.heimdall.llm.api.Provider.Builder("ollama");
        builder.name("Ollama").description("Get up and running with large language models.");
        builder.version("0.7.0").author("Ollama Team").license("MIT")
                .chatFactory(new OllamaChatFactory());
        service.registerProvider(builder.build());
    }
}
