package net.microfalx.heimdall.llm.ollama;

import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Provider;

public class OllamaProviderFactory implements Provider.Factory{

    @Override
    public Provider createProvider() {
        net.microfalx.heimdall.llm.api.Provider.Builder builder = new net.microfalx.heimdall.llm.api.Provider.Builder("ollama");
        builder.name("Ollama").description("Get up and running with large language models.");
        builder.version("0.7.0").author("Ollama Team").license("MIT")
                .chatFactory(new OllamaChatFactory());
        registerModels(builder);
        return builder.build();
    }

    private void registerModels(net.microfalx.heimdall.llm.api.Provider.Builder builder) {
        builder.model(Model.create("gemma3:4b","Gemma3 (4b)")).tag("meta").tag("gemma3").tag("4b");
        builder.model(Model.create("qwen3:0.6b","Qwen3 (0.6b)")).tag("alibaba").tag("qwen3").tag("0.6b");
        builder.model(Model.create("deepseek-r1:7b","DeepSeek-R1 (7b)")).tag("deepseek").tag("deepseek-r1").tag("7b");
    }
}
