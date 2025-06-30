package net.microfalx.heimdall.llm.ollama;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import net.microfalx.heimdall.llm.api.Chat;
import net.microfalx.heimdall.llm.api.LlmNotFoundException;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Prompt;
import net.microfalx.heimdall.llm.core.AbstractChatFactory;
import net.microfalx.lang.StringUtils;

import java.util.ArrayList;

public class OllamaChatFactory extends AbstractChatFactory {

    @Override
    public Chat createChat(Prompt prompt, Model model) {
        if (StringUtils.isEmpty(model.getModelName())) {
            throw new LlmNotFoundException("The model name is required for Ollama");
        }
        StreamingChatModel chatModel = OllamaStreamingChatModel.builder()
                .baseUrl(model.getUri().toASCIIString())
                .modelName(model.getModelName())
                .temperature(model.getTemperature())
                .stop(new ArrayList<>(model.getStopSequences()))
                .topP(model.getTopP()).topK(model.getTopK())
                .responseFormat(ResponseFormat.TEXT)
                .timeout(getProperties().getChatRequestTimeout())
                .build();
        return new OllamaChat(prompt, model).setStreamingChatModel(chatModel);
    }
}
