package net.microfalx.heimdall.protocol.snmp.mib;

import net.microfalx.heimdall.protocol.snmp.jpa.SnmpMibRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class MibMetadataExtractorTest {

    @InjectMocks
    private MibService mibService;

    @Mock
    private SnmpMibRepository snmpMibRepository;

    private MibMetadataExtractor mibMetadataExtractor;
    private MibModule module;

    @BeforeEach
    void setUp() throws Exception {
        mibService.afterPropertiesSet();
    }

    @Test
    void validateMessageOid() {
        module = mibService.findModule("SNMP-FRAMEWORK-MIB");
        mibMetadataExtractor = new MibMetadataExtractor(module);
        mibMetadataExtractor.execute();
        assertNull(mibMetadataExtractor.getEnterpriseOid());
        assertEquals("1.3.6.1.6.3.10.2.1.4", mibMetadataExtractor.getMessageOid());
        assertEquals(null, mibMetadataExtractor.getCreatedAtOid());
        assertNull(mibMetadataExtractor.getSentAtOid());
        assertNull(mibMetadataExtractor.getSeverityOid());
    }

    @Test
    void validateCreateAtOid() {
        module = mibService.findModule("INTERFACETOPN-MIB");
        mibMetadataExtractor = new MibMetadataExtractor(module);
        mibMetadataExtractor.execute();
        assertNull(mibMetadataExtractor.getEnterpriseOid());
        assertNull(mibMetadataExtractor.getMessageOid());
        assertEquals("1.3.6.1.2.1.16.27.1.2.1.12", mibMetadataExtractor.getCreatedAtOid());
        assertEquals(null, mibMetadataExtractor.getSentAtOid());
        assertNull(mibMetadataExtractor.getSeverityOid());
    }

    @Test
    void validateSentAtOid() {
        module = mibService.findModule("SNMPv2-M2M-MIB");
        mibMetadataExtractor = new MibMetadataExtractor(module);
        mibMetadataExtractor.execute();
        assertNull(mibMetadataExtractor.getSeverityOid());
        assertNull(mibMetadataExtractor.getEnterpriseOid());
        assertNull(mibMetadataExtractor.getMessageOid());
        assertNull(mibMetadataExtractor.getCreatedAtOid());
        assertEquals("1.3.6.1.6.3.2.1.2.2.1.5", mibMetadataExtractor.getSentAtOid());
    }

    @Test
    void validateSeverityOid() {
        module = mibService.findModule("SNMP-TARGET-MIB");
        mibMetadataExtractor = new MibMetadataExtractor(module);
        mibMetadataExtractor.execute();
        assertEquals("1.3.6.1.6.3.12.1.3.1.5", mibMetadataExtractor.getSeverityOid());
        assertEquals(null, mibMetadataExtractor.getEnterpriseOid());
        assertNull(mibMetadataExtractor.getMessageOid());
        assertNull(mibMetadataExtractor.getCreatedAtOid());
        assertNull(mibMetadataExtractor.getSentAtOid());
    }


}