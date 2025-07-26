package net.microfalx.heimdall.llm.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.dataset.DataSet;
import net.microfalx.bootstrap.dataset.DataSetRequest;
import net.microfalx.bootstrap.dataset.DataSetUtils;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.Sort;
import net.microfalx.heimdall.llm.api.*;
import net.microfalx.heimdall.llm.core.tools.HelpTool;
import net.microfalx.heimdall.llm.core.tools.SearchTool;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.annotation.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Provider
public class DefaultLlmListener extends ApplicationContextSupport implements LlmListener {

    @Override
    public void onStart(LlmService service) {
        registerPrompts(service);
        registerTools(service);
    }

    private void registerPrompts(LlmService service) {
        service.registerPrompt((Prompt) Prompt.create("default", "Default")
                .system(true).fromResources(StringUtils.EMPTY_STRING).tag(Model.DEFAULT_TAG).build());
    }

    private void registerTools(LlmService llmService) {
        llmService.registerTool((Tool) Tool.builder("search")
                .executor(new SearchTool())
                .parameter((Tool.Parameter) Tool.Parameter.builder("type").description("The type of event").build())
                .description("Searches for events, alerts, and other information")
                .build());
        llmService.registerTool((Tool) Tool.builder("help")
                .executor(new HelpTool())
                .parameter((Tool.Parameter) Tool.Parameter.builder("query").description("A full text search query, supports AND and OR operator").build())
                .description("Searches the integrated help pages")
                .build());
    }

    @Override
    public <M, F extends Field<M>, ID> Page<M> getPage(Chat chat, DataSetRequest<M, F, ID> request) {
        LlmProperties properties = getBean(LlmProperties.class);
        int maximumModelCount = ObjectUtils.defaultIfNull(chat.getPrompt().getMaximumInputEvents(), properties.getMaximumInputEvents());
        Pageable pageable = DataSetUtils.repage(request.getPageable(), maximumModelCount);
        DataSet<M, F, ID> dataSet = request.getDataSet();
        Metadata<M, F, ID> metadata = dataSet.getMetadata();
        Page<M> page = dataSet.findAll(pageable, request.getFilter());
        if (metadata.findTimestampField() != null) {
            Sort sort = Sort.create(Sort.Direction.ASC, metadata.findTimestampField().getName());
            page = DataSetUtils.resort(metadata, pageable, page, sort);
        }
        chat.addFeature(page);
        return page;
    }

}
