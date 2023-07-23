package net.microfalx.heimdall.protocol.snmp.mib;

import org.assertj.core.api.Assertions;
import org.jsmiparser.smi.SmiPrimitiveType;
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
    void describeOid() {
        assertEquals("iso.org.dod.internet.mgmt.mib-2.system.sysUpTime.sysUpTimeInstance", mibService.describeOid("1.3.6.1.2.1.1.3.0"));
        assertEquals("iso.org.dod.internet.mgmt.mib-2.system.sysUpTime.sysUpTimeInstance.23.1", mibService.describeOid("1.3.6.1.2.1.1.3.0.1.23"));
    }

    @Test
    void findVariableName() {
        assertEquals("DISMAN-EVENT-MIB::sysUpTimeInstance", mibService.findName("1.3.6.1.2.1.1.3.0"));
        assertEquals("SNMPv2-MIB::sysUpTime.9", mibService.findName("1.3.6.1.2.1.1.3.9"));
        assertEquals("SNMPv2-MIB::sysUpTime.1.2.3", mibService.findName("1.3.6.1.2.1.1.3.1.2.3"));
        assertEquals("IF-MIB::ifLastChange", mibService.findName("1.3.6.1.2.1.2.2.1.9"));
        assertEquals("IF-MIB::ifLastChange", mibService.findName("1.3.6.1.2.1.2.2.1.9"));
        assertEquals("IF-MIB::ifMIBObjects", mibService.findName("1.3.6.1.2.1.31.1"));
        assertEquals("SNMPv2-SMI::internet", mibService.findName("1.3.6.1"));
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
    void printSymbols() {
        LOGGER.info("Symbols");
        List<MibSymbol> symbols = new ArrayList<>(mibService.getSymbols());
        symbols.sort(Comparator.comparing(MibSymbol::getName));
        for (MibSymbol symbol : symbols) {
            LOGGER.info("  - " + symbol.getName() + "\t\t\t" + symbol.getOid() + "\t\t\t" + symbol.getModule().getName());
        }
    }

    @Test
    void printVariables() {
        LOGGER.info("Variables");
        List<MibVariable> variables = new ArrayList<>(mibService.getVariables());
        variables.sort(Comparator.comparing(MibVariable::getName));
        for (MibVariable variable : variables) {
            LOGGER.info("  - " + variable.getName() + "\t\t\t" + variable.getOid() + "\t\t\t" + variable.getType()
                    + "\t\t\t" + variable.getUnits() + "\t\t\t" + variable.getModule().getName());
            if (variable.getType() == SmiPrimitiveType.ENUM) {
                System.out.println(variable.getVariable());
            }
        }
    }

    @Test
    void loadVariables() {
        MibVariable variable = mibService.findVariable(SnmpConstants.sysUpTime.predecessor().toDottedString());
        assertNotNull(variable);
        assertEquals("sysUpTime", variable.getName());
        assertEquals("SNMPv2-MIB::sysUpTime", variable.getFullName());
        Assertions.assertThat(variable.getDescription()).contains("in hundredths of a second");
    }

}