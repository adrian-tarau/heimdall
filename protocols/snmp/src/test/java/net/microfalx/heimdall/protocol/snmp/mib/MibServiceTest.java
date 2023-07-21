package net.microfalx.heimdall.protocol.snmp.mib;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class MibServiceTest {

    @InjectMocks
    private MibService mibService;

    @BeforeEach
    void setup() throws Exception {
        mibService.afterPropertiesSet();
    }

    @Test
    void loadMibs() {
        assertEquals(80, mibService.getModules().size());
        assertEquals(774, mibService.getVariables().size());
    }

}