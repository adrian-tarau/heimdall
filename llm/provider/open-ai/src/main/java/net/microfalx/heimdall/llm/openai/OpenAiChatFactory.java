package net.microfalx.heimdall.llm.openai;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import net.microfalx.heimdall.llm.api.Chat;
import net.microfalx.heimdall.llm.api.LlmNotFoundException;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Prompt;
import net.microfalx.heimdall.llm.core.AbstractChatFactory;
import net.microfalx.heimdall.llm.core.LlmProperties;
import net.microfalx.lang.StringUtils;

import java.util.ArrayList;

public class OpenAiChatFactory extends AbstractChatFactory {

    private LlmProperties properties = new LlmProperties();

    @Override
    public Chat createChat(Prompt prompt, Model model) {
        if (StringUtils.isEmpty(model.getModelName())) {
            throw new LlmNotFoundException("The model name is required for OpenAI");
        }
        StreamingChatModel chatModel = OpenAiStreamingChatModel.builder()
                .baseUrl(model.getUri().toASCIIString()).apiKey(model.getApyKey())
                .projectId(properties.getOpenAiProjectId())
                .organizationId(properties.getOpenAiOrganizationId())
                .modelName(model.getModelName())
                .temperature(model.getTemperature())
                .maxTokens(model.getMaximumOutputTokens())
                .frequencyPenalty(model.getFrequencyPenalty())
                .presencePenalty(model.getPresencePenalty())
                .maxCompletionTokens(model.getMaximumOutputTokens())
                .responseFormat(model.getResponseFormat().name())
                .stop(new ArrayList<>(model.getStopSequences())).strictTools(true)
                .topP(model.getTopP())
                .build();
        return new OpenAiChat(prompt, model).setStreamingChatModel(chatModel);
    }
}
