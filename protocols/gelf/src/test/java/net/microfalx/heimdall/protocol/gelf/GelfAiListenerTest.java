package net.microfalx.heimdall.protocol.gelf;

import net.microfalx.bootstrap.ai.api.AiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GelfAiListenerTest {

    @Mock
    private AiService aiService;

    @InjectMocks
    private GelfAiListener listener;

    @Test
    void start() {
        listener.onStart(aiService);
        verify(aiService, times(2)).registerPrompt(any());
    }

}