package net.microfalx.heimdall.protocol.gelf;

import net.microfalx.heimdall.llm.api.LlmService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GelfLlmListenerTest {

    @Mock
    private LlmService llmService;

    @InjectMocks
    private GelfLlmListener listener;

    @Test
    void start() {
        listener.onStart(llmService);
        verify(llmService, times(2)).registerPrompt(any());
    }

}