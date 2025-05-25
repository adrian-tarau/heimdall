package net.microflax.heimdall.llm.github;

import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Provider;

@net.microfalx.lang.annotation.Provider
public class GithubProviderFactory implements Provider.Factory {
    @Override
    public Provider createProvider() {
        net.microfalx.heimdall.llm.api.Provider.Builder builder = new net.microfalx.heimdall.llm.api.Provider.Builder("github");
        builder.name("Github").description("a proprietary developer platform that allows developers to create, store, " +
                "manage, and share their code. " +
                "It uses Git to provide distributed version control and GitHub itself provides access control, " +
                "bug tracking, software feature requests, task management, continuous integration, and wikis for " +
                "every project.");
        builder.version("3.16.2").author("Chris Wanstrath, P. J. Hyett, Tom Preston-Werner, and Scott Chacon")
                .license("Proprietary")
                .chatFactory(new GithubChatFactory());
        registerModels(builder);
        return builder.build();
    }

    private void registerModels(net.microfalx.heimdall.llm.api.Provider.Builder builder) {
        // this model gets into a loop
        //builder.model(Model.create("tinyllama", "TinyLlama", "tjake/TinyLlama-1.1B-Chat-v1.0-Jlama-Q4"));
        builder.model((Model.Builder) Model.create("gpt-4o", "GPT-4o")).tag("openai").tag("gpt").tag("4o");
        builder.model((Model.Builder) Model.create("gpt-4.1", "GPT-4.1")).tag("openai").tag("gpt").tag("4.1");
        builder.model((Model.Builder) Model.create("o1", "o1")).tag("openai").tag("o1");
        builder.model((Model.Builder) Model.create("o3", "o3")).tag("openai").tag("o3");
        builder.model((Model.Builder) Model.create("o3-mini", "o3-mini")).tag("openai").tag("o3-mini");
        builder.model((Model.Builder) Model.create("o4-mini", "o4-mini")).tag("openai").tag("o4-mini");

        builder.model((Model.Builder) Model.create("claude_opus_4", "Claude Opus 4"))
                .tag("anthropic pbc").tag("claude").tag("opus").tag("4");
        builder.model((Model.Builder) Model.create("claude_3.5_sonnet", "Claude 3.5 Sonnet"))
                .tag("anthropic pbc").tag("claude").tag("sonnet").tag("3.5");
        builder.model((Model.Builder) Model.create("claude_3.7_sonnet", "Claude 3.7 Sonnet"))
                .tag("anthropic pbc").tag("claude").tag("sonnet").tag("3.7");
        builder.model((Model.Builder) Model.create("claude_3.7_sonnet_thinking", "Claude 3.7 Sonnet Thinking"))
                .tag("anthropic pbc").tag("claude").tag("sonnet thinking").tag("3.5");
        builder.model((Model.Builder) Model.create("claude_sonnet_4", "Claude Sonnet 4"))
                .tag("anthropic pbc").tag("claude").tag("sonnet").tag("4");
        builder.model((Model.Builder) Model.create("gemini_2.0_flash", "Gemini 2.0 Flash"))
                .tag("google").tag("gemini").tag("flash");

    }
}
