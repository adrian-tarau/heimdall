package net.microfalx.heimdall.infrastructure.ping.dataset;

import net.microfalx.bootstrap.core.async.AsynchronousConfig;
import net.microfalx.bootstrap.core.i18n.I18nProperties;
import net.microfalx.bootstrap.core.i18n.I18nService;
import net.microfalx.bootstrap.dataset.Alert;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.heimdall.infrastructure.api.Status;
import net.microfalx.heimdall.infrastructure.ping.system.PingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = {MessageSource.class, I18nService.class})
@Import({I18nProperties.class, AsynchronousConfig.class})
@OverrideAutoConfiguration(enabled = false)
@ImportAutoConfiguration
@SpringBootTest
class StatusAlertProviderTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private I18nService i18nService;

    private StatusAlertProvider<PingResult> alertProvider;
    private Field<PingResult> field;
    private PingResult pingResult = new PingResult();

    @BeforeEach
    void setup() throws Exception {
        i18nService.afterPropertiesSet();
        alertProvider = new StatusAlertProvider<>();
        alertProvider.setApplicationContext(applicationContext);
        field = Mockito.mock(Field.class);
    }

    @Test
    void getForL7Ok() {
        Alert alert = alertProvider.provide(Status.L7OK, field, pingResult);
        assertEquals(Alert.Type.SUCCESS, alert.getType());
        assertNull(alert.getMessage());
    }

    @Test
    void getForL4Con() {
        Alert alert = alertProvider.provide(Status.L4CON, field, pingResult);
        assertEquals(Alert.Type.DANGER, alert.getType());
        assertNotNull(alert.getMessage());
    }

    @Test
    void getForL7TOut() {
        Alert alert = alertProvider.provide(Status.L7TOUT, field, pingResult);
        assertEquals(Alert.Type.WARNING, alert.getType());
        assertNotNull(alert.getMessage());
    }
}