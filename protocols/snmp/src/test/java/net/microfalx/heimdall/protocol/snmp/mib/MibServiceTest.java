package net.microfalx.heimdall.protocol.snmp.mib;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.mp.SnmpConstants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class MibServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MibService.class);

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

    @Test
    void printModules() {
        LOGGER.info("Modules");
        List<MibModule> modules = new ArrayList<>(mibService.getModules());
        modules.sort(Comparator.comparing(MibModule::getName));
        for (MibModule module : modules) {
            LOGGER.info("  - " + module.getName() + "\t\t\t" + module.getOrganization() + "\t\t\t" + module.getVariables().size());
        }
    }

    @Test
    void printVariables() {
        LOGGER.info("Variables");
        List<MibVariable> variables = new ArrayList<>(mibService.getVariables());
        variables.sort(Comparator.comparing(MibVariable::getName));
        for (MibVariable variable : variables) {
            LOGGER.info("  - " + variable.getName() + "\t\t\t" + variable.getOid() + "\t\t\t" + variable.getType().name()
                    + "\t\t\t" + variable.getUnits() + "\t\t\t" + variable.getModule().getName());
        }
    }

    @Test
    void loadVariables() {
        MibVariable variable = mibService.findVariable(SnmpConstants.sysUpTime.predecessor().toDottedString());
        assertNotNull(variable);
        assertEquals("sysUpTime", variable.getName());
        assertEquals("SNMPv2-MIB::sysUpTime", variable.getFullName());
        assertEquals("The time (in hundredths of a second) since the\n" +
                "            network management portion of the system was last\n" +
                "            re-initialized.", variable.getDescription());
    }

}