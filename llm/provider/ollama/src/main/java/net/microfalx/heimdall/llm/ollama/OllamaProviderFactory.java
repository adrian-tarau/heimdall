package net.microfalx.heimdall.llm.ollama;

import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Provider;

@net.microfalx.lang.annotation.Provider
public class OllamaProviderFactory implements Provider.Factory {

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
        builder.model(Model.create("gemma3_4b", "Gemma3 (4b)", "gemma3:4b")).tag("google").tag("gemma");
        builder.model(Model.create("gemma3_1b", "Gemma3 (1b)", "gemma3:1b")).tag("google").tag("gemma");

        builder.model(Model.create("qwen3_0.6b", "Qwen3 (0.6b)", "qwen3:0.6b")).tag("alibaba").tag("qwen3");
        builder.model(Model.create("qwen3_1_7b", "Qwen3 (1.7b)", "qwen3:1.7b")).tag("alibaba").tag("qwen3");
        builder.model(Model.create("qwen3_4b", "Qwen3 (4b)", "qwen3:4b")).tag("alibaba").tag("qwen3");

        builder.model(Model.create("deepseek_r1_7b", "DeepSeek-R1 (7b)", "deepseek-r1:7b")).tag("deepseek").tag("deepseek-r1");
        builder.model(Model.create("deepseek_r1_7b", "DeepSeek-R1 (7b)", "deepseek-r1:7b")).tag("deepseek").tag("deepseek-r1");

        builder.model(Model.create("llama3_2_1b", "Llama 3.2 (1b)", "llama3.2:1b")).tag("meta").tag("llama");
        builder.model(Model.create("llama3_2_3b", "Llama 3.2 (3b)", "llama3.2:3b")).tag("meta").tag("llama");
    }
}
