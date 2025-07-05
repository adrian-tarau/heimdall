package net.microfalx.heimdall.llm.core;

import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Prompt;

import static net.microfalx.heimdall.llm.core.LlmUtils.appendSentence;
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

    private int fragmentCount;

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
        if (prompt.isEmpty()) return properties.getDefaultRole();
        StringBuilder builder = new StringBuilder();
        String role = defaultIfEmpty(prompt.getRole(), properties.getDefaultRole());
        if (isNotEmpty(role)) {
            appendFragment(builder, Prompt.Fragment.ROLE, role);
            if (isNotEmpty(properties.getDefaultGuidanceMessage())) {
                appendSentence(builder, properties.getDefaultGuidanceMessage());
            }
        }
        appendFragment(builder, Prompt.Fragment.INSTRUCTIONS, prompt.getInstructions());
        appendFragment(builder, Prompt.Fragment.EXAMPLES, prompt.getExamples());
        appendFragment(builder, Prompt.Fragment.CONTEXT, prompt.getContext());
        String promptText = builder.toString().trim();
        if (isEmpty(promptText)) promptText = properties.getDefaultRole();
        return promptText;
    }

    private void appendFragment(StringBuilder builder, Prompt.Fragment fragment, String text) {
        if (isEmpty(text)) return;
        String title = service.getTitle(model, prompt, fragment, getDefaultTitle(fragment));
        if (fragmentCount > 0) builder.append(EMPTY_LINE);
        builder.append("# ").append(title).append(EMPTY_LINE);
        text = service.getPromptFragment(model, prompt, fragment, text);
        builder.append(text);
        fragmentCount++;
    }

    private String getDefaultTitle(Prompt.Fragment fragment) {
        return switch (fragment) {
            case ROLE -> "Identity";
            case INSTRUCTIONS -> "Instructions";
            case EXAMPLES -> "Examples";
            case CONTEXT -> "Context";
            default -> "Other";
        };
    }

    private static final String EMPTY_LINE = "\n\n";


}
