package net.microfalx.heimdall.llm.ollama;

import net.microfalx.heimdall.llm.api.Chat;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Provider;
import net.microfalx.heimdall.llm.core.LlmProperties;
import net.microfalx.heimdall.llm.core.LlmServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Iterator;

@ExtendWith(MockitoExtension.class)
class OllamaChatTest {

    @InjectMocks
    private LlmServiceImpl aiService;

    private LlmProperties properties = new LlmProperties();

    private Provider provider;

    @BeforeEach
    void setup() throws Exception {
        properties.setOllamaUri(System.getProperty("ollama.uri", "http://localhost:11434"));
        properties.setOllamaApiKey(System.getProperty("ollama.api_key", "demo"));
        provider = new OllamaProviderFactory().setLlmProperties(properties).createProvider();
    }

    @Test
    void ask() {
        Chat chat = aiService.createChat(loadChat("ollama_gemma3_1b"));
        String response = chat.ask("Tell me a joke about Java");
        System.out.println(response);
        Assertions.assertThat(response.length()).isGreaterThan(0);
    }

    @Test
    void chat() {
        Chat chat = aiService.createChat(loadChat("ollama_gemma3_1b"));
        int tokenCount = 0;
        Iterator<String> stream = chat.chat("Tell me a joke about Java");
        while (stream.hasNext()) {
            String token = stream.next();
            System.out.print(token);
            System.out.flush();
            tokenCount++;
        }
        Assertions.assertThat(tokenCount).isGreaterThan(0);
    }

    private Model loadChat(String modelId) {
        return provider.getModel(modelId);
    }

}