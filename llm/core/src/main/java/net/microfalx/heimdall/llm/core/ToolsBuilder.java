package net.microfalx.heimdall.llm.core;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import net.microfalx.heimdall.llm.api.Chat;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Tool;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * Builds the tools variable for the LLM service.
 */
class ToolsBuilder {

    private static final String NO_TOOLS_AVAILABLE = "No tools available";

    private final LlmServiceImpl service;
    private final Chat chat;
    private final Model model;

    public ToolsBuilder(LlmServiceImpl service, Chat chat) {
        requireNonNull(service);
        requireNonNull(chat);
        this.service = service;
        this.chat = chat;
        this.model = chat.getModel();
    }

    /**
     * Builds the actual tools.
     *
     * @return a non-null instance
     */
    Map<ToolSpecification, dev.langchain4j.service.tool.ToolExecutor> getTools() {
        if (!model.getTags().contains(Model.TOOLS_TAG)) return Collections.emptyMap();
        Map<ToolSpecification, dev.langchain4j.service.tool.ToolExecutor> tools = new HashMap<>();
        for (Tool tool : service.getTools()) {
            ToolSpecification toolSpecification = createToolSpecification(tool);
            ToolExecutor toolExecutor = new ToolExecutor(service, chat, tool);
            tools.put(toolSpecification, toolExecutor);
        }
        return tools;
    }

    /**
     * Builds final prompt text based on available information.
     *
     * @return a non-null string
     */
    String getVariable() {
        if (!model.getTags().contains(Model.TOOLS_TAG)) return NO_TOOLS_AVAILABLE;
        StringBuilder builder = new StringBuilder();
        int index = 1;
        for (Tool tool : service.getTools()) {
            if (!chat.hasTool(tool.getName())) continue;
            builder.append(index).append(". **").append(tool.getName()).append("**: ")
                    .append(tool.getDescription()).append("\n");
            for (Tool.Parameter parameter : tool.getParameters().values()) {
                builder.append(" - **").append(parameter.getName()).append("** (")
                        .append(parameter.getType().name().toLowerCase()).append("): ")
                        .append(parameter.getDescription()).append("\n");
            }
            index++;
        }
        String variableDescription = builder.toString();
        return isNotEmpty(variableDescription) ? variableDescription : NO_TOOLS_AVAILABLE;
    }

    private ToolSpecification createToolSpecification(Tool tool) {
        requireNonNull(tool);
        ToolSpecification.Builder builder = ToolSpecification.builder().name(tool.getName()).description(tool.getDescription()).parameters(createJsonSchema(tool));
        return builder.build();
    }

    private JsonObjectSchema createJsonSchema(Tool tool) {
        requireNonNull(tool);
        JsonObjectSchema.Builder schemaBuilder = JsonObjectSchema.builder();
        for (Tool.Parameter parameter : tool.getParameters().values()) {
            switch (parameter.getType()) {
                case STRING -> schemaBuilder.addStringProperty(parameter.getName(), parameter.getDescription());
                case INTEGER -> schemaBuilder.addIntegerProperty(parameter.getName(), parameter.getDescription());
                case DECIMAL -> schemaBuilder.addNumberProperty(parameter.getName(), parameter.getDescription());
                case BOOLEAN -> schemaBuilder.addBooleanProperty(parameter.getName(), parameter.getDescription());
            }
        }
        return schemaBuilder.build();
    }

}
