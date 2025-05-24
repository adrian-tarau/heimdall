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
     * The duration that models stay loaded in memory (default "5m")
     */
    private Duration interval = Duration.ofMinutes(5);

    /**
     * The OpenAI API key to use when accessing models that require it.
     */
    private String openAiApiKey;

    /**
     * The Ollama API key to use when accessing models that require it.
     */
    private String ollamaApiKey;

    /**
     * The Hugging Face API key to use when accessing models that require it.
     */
    private String huggingFaceApiKey;

    /**
     * The GitHub API key to use when accessing models that require it.
     */
    private String gitHubApiKey;
}
