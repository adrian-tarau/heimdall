package net.microfalx.heimdall.llm.core.provider.jlama;

import net.microfalx.heimdall.llm.api.Chat;
import net.microfalx.heimdall.llm.api.Model;
import net.microfalx.heimdall.llm.api.Provider;
import net.microfalx.heimdall.llm.core.LlmServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Iterator;

@ExtendWith(MockitoExtension.class)
class JLamaChatTest {

    @InjectMocks
    private LlmServiceImpl aiService;

    private Provider provider;

    @BeforeEach
    void setup() throws Exception {
        provider = new JLamaProviderFactory().createProvider();
    }

    @Test
    void ask() {
        Chat chat = aiService.createChat(loadModel("jlama_llama3_2_1b"));
        String response = chat.ask("Tell me a joke about Java");
        System.out.println(response);
        Assertions.assertThat(response.length()).isGreaterThan(0);
    }

    @Test
    void chat() {
        Chat chat = aiService.createChat(loadModel("jlama_llama3_2_1b"));
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

    private Model loadModel(String modelId) {
        return provider.getModel(modelId);
    }

}