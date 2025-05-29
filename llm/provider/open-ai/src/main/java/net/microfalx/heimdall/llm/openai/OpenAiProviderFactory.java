package net.microfalx.heimdall.llm.openai;

import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Provider;

@net.microfalx.lang.annotation.Provider
public class OpenAiProviderFactory implements Provider.Factory {
    @Override
    public Provider createProvider() {
        net.microfalx.heimdall.llm.api.Provider.Builder builder = new net.microfalx.heimdall.llm.api.Provider.Builder("openai");
        builder.name("OpenAI").description("An American artificial intelligence organization founded in December 2015 and " +
                "headquartered in San Francisco, California. It aims to develop safe and beneficial artificial general " +
                "intelligence, which it defines as highly autonomous systems that outperform humans at most economically valuable work.");
        builder.version("4.1").author("John Schulman,Elon Musk,Ilya Sutskever,Sam Altman").license("MIT")
                .chatFactory(new OpenAiChatFactory());
        registerModels(builder);
        return builder.build();
    }

    private void registerModels(net.microfalx.heimdall.llm.api.Provider.Builder builder) {
        builder.model(Model.create("o4-mini", "O4 Mini", "o4-mini-2025-04-16")).tag("openai").tag("mini");
        builder.model(Model.create("o3", "O3", "o3-2025-04-16")).tag("openai").tag("o3");
        builder.model(Model.create("o3-mini", "O3 Mini", "o3-mini-2025-01-31")).tag("openai").tag("mini");
        builder.model(Model.create("o1", "O1", "o1-2024-12-17")).tag("openai").tag("o1");
        builder.model(Model.create("gpt_4.1", "GPT 4.1", "gpt-4.1-2025-04-14")).tag("openai").tag("gpt");
        builder.model(Model.create("gpt-4o", "GPT 4o", "gpt-4o-2024-08-06")).tag("openai").tag("gpt");
        builder.model(Model.create("gpt_4.1_mini", "GPT 4.1 Mini", "gpt-4.1-mini-2025-04-14")).tag("openai").tag("gpt").tag("mini");
        builder.model(Model.create("gpt_4o_mini", "GPT 4o Mini", "gpt-4o-mini-2024-07-18")).tag("openai").tag("gpt").tag("mini");
        builder.model(Model.create("gpt_4.1_nano", "GPT 4.1 Nano", "gpt-4.1-nano-2025-04-14")).tag("openai").tag("gpt").tag("nano");
    }
}
