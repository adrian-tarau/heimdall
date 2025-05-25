package net.microfalx.heimdall.llm.ollama;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import net.microfalx.heimdall.llm.api.Chat;
import net.microfalx.heimdall.llm.api.LlmNotFoundException;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.lang.NumberUtils;
import net.microfalx.lang.StringUtils;

import java.util.ArrayList;

public class OllamaChatFactory implements Chat.Factory {

    @Override
    public Chat createChat(Model model) {
        if (StringUtils.isEmpty(model.getModelName())) {
            throw new LlmNotFoundException("The model name is required for Ollama");
        }
        StreamingChatModel chatModel = OllamaStreamingChatModel.builder()
                .modelName(model.getModelName())
                .temperature(NumberUtils.toFloat(model.getTemperature()).doubleValue())
                .baseUrl(model.getUri().toASCIIString())
                .responseFormat(ResponseFormat.TEXT)
                .stop(new ArrayList<>(model.getStopSequences()))
                .topP(model.getTopP())
                .topK(model.getTopK())
                .build();
        return new OllamaChat(model).setStreamingChatModel(chatModel);
    }
}
