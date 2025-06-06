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
     * Show additional debug information related to the LLM operations.
     */
    private boolean debug;

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
}
