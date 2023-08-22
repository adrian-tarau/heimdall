package net.microfalx.heimdall.protocol.snmp.mib;

import net.microfalx.heimdall.protocol.snmp.jpa.SnmpMibRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void validateSnmpMib() {
        module = mibService.findModule("snmpv2_mib");
        mibMetadataExtractor = new MibMetadataExtractor(module);
        mibMetadataExtractor.execute();
        assertNull(mibMetadataExtractor.getEnterpriseOid());
        assertNull(mibMetadataExtractor.getMessageOid());
        assertNull(mibMetadataExtractor.getCreatedAtOid());
        assertNull(mibMetadataExtractor.getSentAtOid());
        assertNull(mibMetadataExtractor.getSeverityOid());
    }

    @Test
    void validateSnmpTargetMib() {
        module = mibService.findModule("snmp_target_mib");
        mibMetadataExtractor = new MibMetadataExtractor(module);
        mibMetadataExtractor.execute();
        assertNull(mibMetadataExtractor.getEnterpriseOid());
        assertNull(mibMetadataExtractor.getMessageOid());
        assertNull(mibMetadataExtractor.getCreatedAtOid());
        assertNull(mibMetadataExtractor.getSentAtOid());
        assertNull(mibMetadataExtractor.getSeverityOid());
    }


    @Test
    void validateIanaItuAlarmTcMib() {
        module = mibService.findModule("iana_printer_mib");
        mibMetadataExtractor = new MibMetadataExtractor(module);
        mibMetadataExtractor.execute();
        assertNull(mibMetadataExtractor.getSeverityOid());
        assertNull(mibMetadataExtractor.getEnterpriseOid());
        assertNull(mibMetadataExtractor.getMessageOid());
        assertNull(mibMetadataExtractor.getCreatedAtOid());
        assertNull(mibMetadataExtractor.getSentAtOid());
    }


}