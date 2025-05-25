package net.microfalx.heimdall.llm.openai;

import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Provider;

public class OpenAiProviderFactory implements Provider.Factory {
    @Override
    public Provider createProvider() {
        net.microfalx.heimdall.llm.api.Provider.Builder builder = new net.microfalx.heimdall.llm.api.Provider.Builder("ollama");
        builder.name("OpenAI").description("An American artificial intelligence organization founded in December 2015 and " +
                "headquartered in San Francisco, California. It aims to develop safe and beneficial artificial general " +
                "intelligence, which it defines as highly autonomous systems that outperform humans at most economically valuable work.");
        builder.version("4.1").author("John Schulman,Elon Musk,Ilya Sutskever,Sam Altman").license("MIT")
                .chatFactory(new OpenAiChatFactory());
        registerModels(builder);
        return builder.build();
    }

    private void registerModels(net.microfalx.heimdall.llm.api.Provider.Builder builder) {
        builder.model((Model.Builder) Model.create("openai o4-mini", "o4-mini")).tag("openai").tag("o4-mini");
        builder.model((Model.Builder) Model.create("openai o3", "o3")).tag("openai").tag("o3");
        builder.model((Model.Builder) Model.create("openai o3-mini", "o3-mini")).tag("openai").tag("o3-mini");
        builder.model((Model.Builder) Model.create("openai o1", "o1")).tag("openai").tag("o1");
        builder.model((Model.Builder) Model.create("openai gpt-4.1", "GPT-4.1")).tag("openai").tag("gpt").tag("4.1");
        builder.model((Model.Builder) Model.create("openai gpt-4o", "GPT-4o")).tag("openai").tag("gpt").tag("4o");
        builder.model((Model.Builder) Model.create("openai gpt-4.1 mini", "GPT-4.1 mini")).tag("openai").tag("gpt").tag("4.1 mini");
        builder.model((Model.Builder) Model.create("openai gpt-4o mini", "GPT-4o mini")).tag("openai").tag("gpt").tag("4o mini");
        builder.model((Model.Builder) Model.create("openai gpt-4.1 nano", "GPT-4.1 nano")).tag("openai").tag("gpt").tag("4.1 nao");
    }
}
