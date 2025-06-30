package net.microfalx.heimdall.protocol.smtp.simulator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApacheMBoxDataSetTest {

    private static ApacheMBoxDataSet dataSet;
    private static Iterator<MimeMessage> messages;

    @BeforeAll
    static void setUp() {
        dataSet = (ApacheMBoxDataSet) new ApacheMBoxDataSet.Factory().createDataSet();
        messages = dataSet.iterator();
    }

    @Test
    void getOne() {
        assertTrue(messages.hasNext());
        assertMimeMessage(messages.next());
    }

    @Test
    void hundred() {
        for (int i = 0; i < 100; i++) {
            assertTrue(messages.hasNext());
            MimeMessage message = messages.next();
            assertMimeMessage(message);
        }
    }

    private void assertMimeMessage(MimeMessage message) {
        assertNotNull(message);
        assertNotNull(message.getMailbox());
        assertNotNull(message.getContent());
        assertTrue(message.getIndex() > 0);
    }

}