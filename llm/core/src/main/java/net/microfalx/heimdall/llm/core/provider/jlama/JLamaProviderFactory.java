package net.microfalx.heimdall.llm.core.provider.jlama;

import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Provider;
import net.microfalx.heimdall.llm.core.AbstractProviderFactory;

@net.microfalx.lang.annotation.Provider
public class JLamaProviderFactory extends AbstractProviderFactory {

    @Override
    public Provider createProvider() {
        net.microfalx.heimdall.llm.api.Provider.Builder builder = new net.microfalx.heimdall.llm.api.Provider.Builder("jlama");
        builder.name("JLama").description("A LLM inference engine for Java");
        builder.version("0.1.0").author("Jake Luciani").license("Apache-2.0")
                .chatFactory(new JLamaChatFactory());
        registerModels(builder);
        return builder.build();
    }

    private void registerModels(net.microfalx.heimdall.llm.api.Provider.Builder builder) {
        builder.model((Model.Builder) Model.create("llama3.2-1b", "Llama 3.2 (1B)",
                        "tjake/Llama-3.2-1B-Instruct-JQ4").maximumContextLength(8_192)
                .setDefault(true).tag("meta").tag("llama").tag("1b").tag("llama3"));
        builder.model((Model.Builder) Model.create("llama3.2-3b", "Llama 3.2 (3B)",
                        "tjake/Llama-3.2-3B-Instruct-JQ4").maximumContextLength(8_192)
                .tag("meta").tag("llama").tag("3b").tag("llama3"));
        builder.model((Model.Builder) Model.create("gemma2-2b", "Gemma 3 (2B)",
                        "tjake/gemma-2-2b-it-JQ4").maximumContextLength(8_192)
                .tag("google").tag("gemma").tag("2b"));
        builder.model(Model.create("mistral-7B", "Mistral (7B)",
                        "tjake/Mistral-7B-Instruct-v0.3-Jlama-Q4").maximumContextLength(8_192))
                .tag("mistral").tag("7b");
        builder.model((Model.Builder) Model.create("qwen2.5-0.5B", "Qwen 2.5 (0.5B)",
                        "tjake/Qwen2.5-0.5B-Instruct-JQ4").maximumContextLength(8_192)
                .tag("alibaba").tag("qwen").tag("0.5b"));

        builder.model((Model.Builder) Model.create("e5-base-v2", "E5 Base",
                        "intfloat/e5-base-v2").maximumContextLength(1_024)
                .forEmbedding().tag("embedding").tag("microsoft"));
        builder.model((Model.Builder) Model.create("e5-small-v2", "E5 Small",
                        "intfloat/e5-small-v2").maximumContextLength(1_024)
                .embedding(true).tag("embedding").tag("microsoft"));

    }
}
