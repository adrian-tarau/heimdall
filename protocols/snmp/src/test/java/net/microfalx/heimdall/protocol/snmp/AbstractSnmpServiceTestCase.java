package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.heimdall.protocol.snmp.mib.MibService;
import net.microfalx.heimdall.protocol.snmp.mib.MibVariable;
import net.microfalx.lang.JvmUtils;
import net.microfalx.resource.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractSnmpServiceTestCase {

    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    protected MibService mibService;

    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    protected MibVariable mibVariable;

    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    protected ResourceService resourceService;

    @Spy
    protected SnmpProperties properties = new SnmpProperties();

    protected SnmpTestHelper helper;

    @InjectMocks
    protected SnmpService snmpService;

    @BeforeEach
    void setup() throws Exception {
        helper = new SnmpTestHelper(properties);
        properties.setTcpPort(helper.getNextPort());
        properties.setUdpPort(helper.getNextPort());
        initMocks();
        snmpService.afterPropertiesSet();
    }

    @AfterEach
    void destroy() {
        snmpService.destroy();
    }

    private void initMocks() {
        Resource directory = Resource.directory(JvmUtils.getTemporaryDirectory("snmp", null));
        when(resourceService.getShared(anyString())).thenReturn(directory);
    }
}
