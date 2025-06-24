package net.microfalx.heimdall.llm.core;

import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Prompt;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.*;

/**
 * Builds the system message based on the provided model and prompt.
 */
class SystemMessageBuilder {

    private final LlmServiceImpl service;
    private final LlmProperties properties;
    private final Model model;
    private final Prompt prompt;

    SystemMessageBuilder(LlmServiceImpl service, Model model, Prompt prompt) {
        requireNonNull(service);
        requireNonNull(model);
        requireNonNull(prompt);
        this.service = service;
        this.properties = service.getProperties();
        this.model = model;
        this.prompt = prompt;
    }

    /**
     * Builds final prompt text based on available information.
     *
     * @return a non-null string
     */
    String build() {
        StringBuilder builder = new StringBuilder();
        String role = defaultIfEmpty(prompt.getRole(), properties.getDefaultRole());
        if (isNotEmpty(role)) {
            role = service.getPromptFragment(model, prompt, Prompt.Fragment.ROLE, role);
            LlmUtils.appendSentence(builder, role);
            if (isNotEmpty(properties.getDefaultGuidanceMessage())) {
                LlmUtils.appendSentence(builder, properties.getDefaultGuidanceMessage());
            }
            newParagraph(builder);
        }
        if (isNotEmpty(prompt.getContext())) {
            String context = service.getPromptFragment(model, prompt, Prompt.Fragment.CONTEXT, prompt.getContext());
            if (isNotEmpty(context)) {
                LlmUtils.appendSentence(builder, context).append("\n");
            }
            newParagraph(builder);
        }
        if (isNotEmpty(prompt.getExamples())) {
            String context = service.getPromptFragment(model, prompt, Prompt.Fragment.EXAMPLES, prompt.getExamples());
            if (isNotEmpty(context)) {
                LlmUtils.appendSentence(builder, context).append("\n");
            }
            newParagraph(builder);
        }
        String promptText = builder.toString().trim();
        if (isEmpty(promptText)) promptText = properties.getDefaultRole();
        return promptText;
    }

    private void newParagraph(StringBuilder builder) {
        if (!builder.isEmpty() && !builder.toString().endsWith("\n\n")) {
            builder.append("\n\n");
        }
    }


}
