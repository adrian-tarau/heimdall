package net.microfalx.heimdall.llm.ollama;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Provider;
import net.microfalx.heimdall.llm.core.LlmProperties;
import net.microfalx.lang.UriUtils;

@net.microfalx.lang.annotation.Provider
@Setter
@Getter
public class OllamaProviderFactory implements Provider.Factory {

    private LlmProperties llmProperties;

    @Override
    public Provider createProvider() {
        net.microfalx.heimdall.llm.api.Provider.Builder builder = new net.microfalx.heimdall.llm.api.Provider.Builder("ollama");
        if (llmProperties != null) {
            builder.uri(UriUtils.parseUri(llmProperties.getOllamaUri()), llmProperties.getOllamaApiKey());
        }
        builder.name("Ollama").description("Get up and running with large language models.");
        builder.version("0.7.0").author("Ollama Team").license("MIT")
                .chatFactory(new OllamaChatFactory()).tag("ollama");
        registerModels(builder);
        return builder.build();
    }

    private void registerModels(net.microfalx.heimdall.llm.api.Provider.Builder builder) {
        builder.model((Model.Builder) Model.create("gemma3_4b", "Gemma3 (4b)", "gemma3:4b").tag("google").tag("gemma"));
        builder.model((Model.Builder) Model.create("gemma3_1b", "Gemma3 (1b)", "gemma3:1b").tag("google").tag("gemma"));

        builder.model((Model.Builder) Model.create("qwen3_0.6b", "Qwen3 (0.6b)", "qwen3:0.6b").tag("alibaba").tag("qwen"));
        builder.model((Model.Builder) Model.create("qwen3_1_7b", "Qwen3 (1.7b)", "qwen3:1.7b").tag("alibaba").tag("qwen"));
        builder.model((Model.Builder) Model.create("qwen3_4b", "Qwen3 (4b)", "qwen3:4b").tag("alibaba").tag("qwen"));

        builder.model((Model.Builder) Model.create("deepseek_r1_7b", "DeepSeek-R1 (7b)", "deepseek-r1:7b").tag("deepseek"));
        builder.model((Model.Builder) Model.create("deepseek_r1_7b", "DeepSeek-R1 (7b)", "deepseek-r1:7b").tag("deepseek"));

        builder.model((Model.Builder) Model.create("llama3_2_1b", "Llama 3.2 (1b)", "llama3.2:1b").tag("meta").tag("llama"));
        builder.model((Model.Builder) Model.create("llama3_2_3b", "Llama 3.2 (3b)", "llama3.2:3b").tag("meta").tag("llama"));
    }
}
