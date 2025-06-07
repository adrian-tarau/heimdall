package net.microflax.heimdall.llm.github;

import com.azure.ai.inference.models.ChatCompletionsResponseFormatText;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.github.GitHubModelsStreamingChatModel;
import net.microfalx.heimdall.llm.api.Chat;
import net.microfalx.heimdall.llm.api.LlmNotFoundException;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Prompt;
import net.microfalx.heimdall.llm.core.AbstractChatFactory;
import net.microfalx.lang.StringUtils;

import java.util.ArrayList;


public class GithubChatFactory extends AbstractChatFactory {
    @Override
    public Chat createChat(Prompt prompt, Model model) {
        if (StringUtils.isEmpty(model.getModelName())) {
            throw new LlmNotFoundException("The model name is required for Github");
        }
        StreamingChatModel chatModel = GitHubModelsStreamingChatModel.builder()
                .modelName(model.getModelName()).temperature(model.getTemperature())
                .frequencyPenalty(model.getFrequencyPenalty())
                .presencePenalty(model.getPresencePenalty()).topP(model.getTopP())
                .stop(new ArrayList<>(model.getStopSequences())).gitHubToken(model.getApyKey())
                .responseFormat(new ChatCompletionsResponseFormatText())
                .maxTokens(model.getMaximumOutputTokens())
                .build();
        return new GithubChat(prompt, model).setStreamingChatModel(chatModel);
    }
}
