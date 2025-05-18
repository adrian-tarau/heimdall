package net.microfalx.heimdall.llm.jlama;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.jlama.JlamaStreamingChatModel;
import net.microfalx.heimdall.llm.api.AiNotFoundException;
import net.microfalx.heimdall.llm.api.Chat;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.core.AbstractChatFactory;
import net.microfalx.lang.NumberUtils;
import net.microfalx.lang.StringUtils;

public class JLamaChatFactory extends AbstractChatFactory {

    @Override
    public Chat createChat(Model model) {
        if (StringUtils.isEmpty(model.getModelName())) {
            throw new AiNotFoundException("The model name is required for JLama");
        }
        StreamingChatModel chatModel = JlamaStreamingChatModel.builder()
                .modelName(model.getModelName()).temperature(NumberUtils.toFloat(model.getTemperature()))
                .maxTokens(model.getMaximumOutputTokens())
                .modelCachePath(getModelCacheDirectory("jlama").toPath())
                .workingDirectory(getWorkingDirectory("jlama").toPath())
                .build();
        return new JLamaChat(model).setStreamingChatModel(chatModel);
    }

}
