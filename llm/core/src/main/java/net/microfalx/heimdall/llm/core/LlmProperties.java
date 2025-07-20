package net.microfalx.heimdall.llm.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties("heimdall.llm")
@Getter
@Setter
public class LlmProperties {

    /**
     * Indicates whether the LLM service is enabled. If set to false, no LLM operations will be performed.
     */
    private boolean enabled = true;

    /**
     * Indicates whether the LLM service should use an embedding store to cache embeddings.
     */
    private boolean embeddingEnabled = false;

    /**
     * Show additional debug information related to the LLM operations.
     */
    private boolean debugEnabled;

    /**
     * Indicates whether the LLM service should persist data to the database.
     */
    private boolean persistenceEnabled = true;

    /**
     * The maximum number of parallel requests each model can process simultaneously. By default, this
     * is set to 4 (or 1, depending on memory availability)
     */
    private int maximumConcurrentRequests = 4;

    /**
     * The maximum number of input events that can be processed in a single request. This is used to limit
     * the size of the input to the LLM models.
     */
    private int maximumInputEvents = 100;

    /**
     * Indicates whether the LLM service has access to the internet.
     */
    private boolean offline;

    /**
     * The duration that models stay loaded in memory (default "5m")
     */
    private Duration modelCache = Duration.ofMinutes(5);

    /**
     * The duration that a chat can stay active
     */
    private Duration chatTimeout = Duration.ofMinutes(15);

    /**
     * The interval to auto-save chat messages
     */
    private Duration chatAutoSaveInterval = Duration.ofSeconds(30);

    /**
     * The duration of a chat request before it times out.
     */
    private Duration chatRequestTimeout = Duration.ofSeconds(60);

    /**
     * The default provider to use instead of the one selected.
     */
    private String defaultProvider;

    /**
     * The default model, override to be used.
     */
    private String defaultModel;

    /**
     * OpenAI API key to use when accessing models that require it.
     */
    private String openAiUri = "https://api.openai.com/v1";

    /**
     * OpenAI API key to use when accessing models that require it.
     */
    private String openAiApiKey;

    /**
     * The id of the project
     */
    private String openAiProjectId;

    /**
     * The id of the organization
     */
    private String openAiOrganizationId;

    /**
     * Ollama API key to use when accessing models that require it.
     */
    private String ollamaApiKey;

    /**
     * Ollama URI to use when accessing models that require it.
     */
    private String ollamaUri = "http://localhost:11434";

    /**
     * Hugging Face API key to use when accessing models that require it.
     */
    private String huggingFaceApiKey;

    /**
     * GitHub API key to use when accessing models that require it.
     */
    private String gitHubApiKey;

    /**
     * The default temperature to use when generating text.
     */
    private Double defaultTemperature = 0.1;

    /**
     * The default top P to use when generating text.
     */
    private Double defaultTopP = 0.9;

    /**
     * The default top K to use when generating text.
     */
    private Integer defaultTopK = 20;

    /**
     * The default role (prompt) to use when creating the prompt.
     */
    private String defaultRole = "You are a helpful assistant.";

    /**
     * The default guidance message to use when creating the prompt.
     */
    private String defaultGuidanceMessage = "";

    /**
     * A question to summarize the conversation in a few words.
     */
    private String summaryWords = "Summarize the conversation in a few words." +
            " The summary should be concise and to the point, capturing the essence of the conversation." +
            " Do not include any personal opinions or interpretations, just the facts discussed in the conversation.";

    /**
     * A question to summarize the conversation in a short sentence.
     */
    private String summarySentence = "Summarize the conversation in a single sentence." +
            " The summary should be concise and to the point, capturing the essence of the conversation." +
            " Do not include any personal opinions or interpretations, just the facts discussed in the conversation.";
}
