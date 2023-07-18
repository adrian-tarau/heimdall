package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.heimdall.protocol.core.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Iterator;

import static net.microfalx.lang.ThreadUtils.sleepSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class SmtpServerTest {

    @Mock
    private SmtpService smtpService;

    @Spy
    private SmtpProperties configuration = new SmtpProperties();

    @InjectMocks
    private SmtpServer serverService;

    private SmtpTestHelper helper;

    @BeforeEach
    void setup() {
        helper = new SmtpTestHelper(configuration);
        configuration.setPort(helper.getNextPort());
        serverService.initialize();
    }

    @Test
    void initialize() {
        assertNotNull(serverService);
        serverService.initialize();
    }

    @Test
    void sendText() throws IOException {
        helper.sendEmail(false);
        sleepSeconds(2);
        assertEvents(false);
    }

    @Test
    void sendHtml() throws IOException {
        helper.sendEmail(true);
        sleepSeconds(2);
        assertEvents(true);
    }

    @Test
    void sendHtmlWithAttachments() throws IOException {
        helper.addAttachment("file1.txt", "hi");
        helper.addAttachment("file2.txt", "how");
        helper.addAttachment("file3.txt", "sleep");
        helper.sendEmail(true);
        sleepSeconds(2);
        SmtpEvent smtpEvent = assertEvents(true);
        assertEquals(4, smtpEvent.getParts().size());
    }


    SmtpEvent assertEvents(boolean html) throws IOException {
        ArgumentCaptor<SmtpEvent> smtpCapture = ArgumentCaptor.forClass(SmtpEvent.class);
        verify(smtpService, times(1)).accept(smtpCapture.capture());
        Iterator<SmtpEvent> iterator = smtpCapture.getAllValues().iterator();
        SmtpEvent message = iterator.next();
        //assertEquals("Test Email", message.getName());
        if (html) {
           //assertEquals("Test message", message.getBodyAsString());
        } else {
          //  assertEquals("Test html", message.getBodyAsString());
        }
        assertNotNull(message.getSource().getValue());
        assertNotNull(message.getCreatedAt());
        assertNotNull(message.getSentAt());
        assertNotNull(message.getReceivedAt());
        assertEquals(Severity.INFO, message.getSeverity());
        return message;

    }

}