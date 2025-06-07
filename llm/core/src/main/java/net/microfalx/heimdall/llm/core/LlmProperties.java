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
     * is set to 4 (or 1, depending on memory availability),
     */
    private int maximumConcurrentRequests = 4;

    /**
     * Indicates whether the LLM service has access to the internet.
     */
    private boolean offline;

    /**
     * The duration that models stay loaded in memory (default "5m")
     */
    private Duration interval = Duration.ofMinutes(5);


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
    private String defaultGuidanceMessage = "Answer the question as best you can. If you don't know the answer, just say that you don't know. Don't try to make up an answer.";
}
