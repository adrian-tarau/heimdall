package net.microfalx.heimdall.llm.huggingface;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import net.microfalx.heimdall.llm.api.Chat;
import net.microfalx.heimdall.llm.api.LlmNotFoundException;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.core.AbstractChatFactory;
import net.microfalx.lang.StringUtils;

public class HuggingFaceChatFactory extends AbstractChatFactory {

    @Override
    public Chat createChat(Model model) {
        if (StringUtils.isEmpty(model.getModelName())) {
            throw new LlmNotFoundException("The model name is required for HuggingFace");
        }
        ChatModel chatModel = HuggingFaceChatModel.builder()
                .accessToken(model.getApyKey())
                .baseUrl(model.getUri().toASCIIString())
                .returnFullText(true)
                .modelId(model.getId())
                .temperature(model.getTemperature())
                .maxNewTokens(model.getMaximumOutputTokens())
                .build();
        return new HuggingFaceChat(model).setChatModel(chatModel);
    }
}
