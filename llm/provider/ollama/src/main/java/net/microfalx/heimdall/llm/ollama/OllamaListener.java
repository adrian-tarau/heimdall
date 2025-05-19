package net.microfalx.heimdall.llm.ollama;

import net.microfalx.heimdall.llm.api.AiListener;
import net.microfalx.heimdall.llm.api.AiService;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.lang.annotation.Provider;

@Provider
public class OllamaListener implements AiListener {

    @Override
    public void registerProviders(AiService service) {
        net.microfalx.heimdall.llm.api.Provider.Builder builder = new net.microfalx.heimdall.llm.api.Provider.Builder("ollama");
        builder.name("Ollama").description("Get up and running with large language models.");
        builder.version("0.7.0").author("Ollama Team").license("MIT")
                .chatFactory(new OllamaChatFactory());
        registerModels(builder);
        service.registerProvider(builder.build());
    }

    private void registerModels(net.microfalx.heimdall.llm.api.Provider.Builder builder) {
        builder.model(Model.create("gemma3:4b","Gemma3 (4b)","/gemma3:4b"));
        builder.model(Model.create("qwen3:0.6b","Qwen3 (0.6b)"));
        builder.model(Model.create("deepseek-r1:7b","DeepSeek-R1 (7b)"));
    }
}
