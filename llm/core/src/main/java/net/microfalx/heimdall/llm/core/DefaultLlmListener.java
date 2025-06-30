package net.microfalx.heimdall.llm.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.dataset.DataSetRequest;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.heimdall.llm.api.Chat;
import net.microfalx.heimdall.llm.api.LlmListener;
import net.microfalx.heimdall.llm.api.LlmService;
import net.microfalx.heimdall.llm.api.Prompt;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.annotation.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Provider
public class DefaultLlmListener extends ApplicationContextSupport implements LlmListener {

    @Override
    public void onStart(LlmService service) {
        registerPrompts(service);
    }

    private void registerPrompts(LlmService service) {
        Prompt prompt = (Prompt) Prompt.create("summary", "Summary")
                .question("Summarize the current data")
                .tag("summary")
                .description("Creates a summary of the current data")
                .build();
        service.registerPrompt(prompt);
    }

    @Override
    public <M, F extends Field<M>, ID> Page<M> getPage(Chat chat, DataSetRequest<M, F, ID> request) {
        LlmProperties properties = getBean(LlmProperties.class);
        Integer maximumModelCount = ObjectUtils.defaultIfNull(chat.getPrompt().getMaximumInputEvents(), properties.getMaximumInputEvents());
        Pageable pageable = Pageable.ofSize(maximumModelCount);
        return request.getDataSet().findAll(pageable, request.getFilter());
    }

}
