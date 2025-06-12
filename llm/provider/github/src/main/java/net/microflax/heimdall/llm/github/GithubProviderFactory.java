package net.microflax.heimdall.llm.github;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Provider;
import net.microfalx.heimdall.llm.core.AbstractProviderFactory;

@net.microfalx.lang.annotation.Provider
@Getter
@Setter
public class GithubProviderFactory extends AbstractProviderFactory {

    @Override
    public Provider createProvider() {
        net.microfalx.heimdall.llm.api.Provider.Builder builder = new net.microfalx.heimdall.llm.api.Provider.Builder("github");
        builder.apyKey(getProperties().getGitHubApiKey());
        builder.name("Github").description("A proprietary developer platform that allows developers to create, store, " +
                "manage, and share their code");
        builder.version("3.16.2").author("Microsoft").license("Proprietary")
                .chatFactory(new GithubChatFactory()).tag("github");
        registerModels(builder);
        return builder.build();
    }

    private void registerModels(net.microfalx.heimdall.llm.api.Provider.Builder builder) {
        builder.model((Model.Builder) Model.create("gpt_4o", "GPT 4o", "GPT-4o")
                .maximumContextLength(128_000)
                .tag("openai").tag("gpt"));
        builder.model((Model.Builder) Model.create("gpt_4.1", "GPT 4.1", "GPT-4.1")
                        .maximumContextLength(128_000))
                .tag("openai").tag("gpt");
        builder.model((Model.Builder) Model.create("o1", "O1", "o1")
                .maximumContextLength(128_000)
                .tag("openai"));
        builder.model((Model.Builder) Model.create("o3", "O3", "o3")
                .maximumContextLength(128_000)
                .tag("openai"));
        builder.model((Model.Builder) Model.create("o3_mini", "O3 Mini", "o3-mini")
                .maximumContextLength(128_000)
                .maximumContextLength(128_000).tag("openai").tag("mini"));
        builder.model((Model.Builder) Model.create("o4_mini", "O4 Mini", "o4-mini")
                .maximumContextLength(128_000)
                .tag("openai").tag("mini"));

        builder.model((Model.Builder) Model.create("claude_opus_4", "Claude Opus 4",
                        "Claude Opus 4").maximumContextLength(200_000)
                .tag("anthropic").tag("claude").tag("opus"));
        builder.model((Model.Builder) Model.create("claude_3.5_sonnet", "Claude 3.5 Sonnet",
                        "Claude 3.5 Sonnet").maximumContextLength(200_000)
                .tag("anthropic").tag("claude").tag("sonnet"));
        builder.model((Model.Builder) Model.create("claude_3.7_sonnet", "Claude 3.7 Sonnet",
                        "Claude 3.7 Sonnet").maximumContextLength(200_000)
                .tag("anthropic").tag("claude").tag("sonnet"));
        builder.model((Model.Builder) Model.create("claude_3.7_sonnet_thinking", "Claude 3.7 Sonnet Thinking",
                        "Claude 3.7 Sonnet Thinking").maximumContextLength(200_000)
                .tag("anthropic").tag("claude").tag("sonnet").tag("thinking"));
        builder.model((Model.Builder) Model.create("claude_sonnet_4", "Claude Sonnet 4", "Claude Sonnet 4").maximumContextLength(200_000)
                .tag("anthropic").tag("claude").tag("sonnet"));
        builder.model((Model.Builder) Model.create("gemini_2.0_flash", "Gemini 2.0 Flash",
                        "Gemini 2.0 Flash").maximumContextLength(1_000_000)
                .tag("google").tag("gemini").tag("flash"));

    }
}
