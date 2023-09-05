package net.microfalx.heimdall.protocol.snmp.mib;

import net.microfalx.bootstrap.test.AbstractBootstrapServiceTestCase;
import net.microfalx.heimdall.protocol.snmp.jpa.SnmpMib;
import net.microfalx.heimdall.protocol.snmp.jpa.SnmpMibRepository;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.Resource;
import org.assertj.core.api.Assertions;
import org.jsmiparser.smi.SmiPrimitiveType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.mp.SnmpConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {MibService.class})
class MibServiceTest extends AbstractBootstrapServiceTestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(MibService.class);

    private final AtomicLong SEQUENCE = new AtomicLong(1);
    private final List<SnmpMib> persistedMibs = new ArrayList<>();

    @MockBean
    private SnmpMibRepository snmpMibRepository;

    @Autowired
    private MibService mibService;

    @BeforeEach
    void setup() throws Exception {
        when(snmpMibRepository.saveAndFlush(any(SnmpMib.class)))
                .then(invocation -> {
                    SnmpMib snmpMib = invocation.getArgument(0);
                    persistedMibs.add(snmpMib);
                    snmpMib.setId(SEQUENCE.getAndIncrement());
                    return snmpMib;
                });
        doReturn(persistedMibs).when(snmpMibRepository).findAll();
    }

    @Test
    void loadSystemMibs() {
        assertEquals(331, mibService.getModules().size());
        assertEquals(17082, mibService.getVariables().size());
    }

    @Test
    void loadUserMibs() {
        Resource simulatorMibs = ClassPathResource.directory("simulator/snmp/mib");
        mibService.loadModules(simulatorMibs);
        assertEquals(329, mibService.getModules().size());
        assertEquals(17082, mibService.getVariables().size());
    }

    @Test
    void describeOid() {
        assertEquals("iso.org.dod.internet.mgmt.mib-2.system.sysUpTime.sysUpTimeInstance", mibService.describeOid("1.3.6.1.2.1.1.3.0"));
        assertEquals("iso.org.dod.internet.mgmt.mib-2.system.sysUpTime.sysUpTimeInstance.23.1", mibService.describeOid("1.3.6.1.2.1.1.3.0.1.23"));
    }

    @Test
    void findVariableName() {
        assertEquals("DISMAN-EXPRESSION-MIB::sysUpTimeInstance", mibService.findName("1.3.6.1.2.1.1.3.0"));
        assertEquals("RFC1213-MIB::sysUpTime.9", mibService.findName("1.3.6.1.2.1.1.3.9"));
        assertEquals("RFC1213-MIB::sysUpTime.1.2.3", mibService.findName("1.3.6.1.2.1.1.3.1.2.3"));
        assertEquals("IF-MIB::ifLastChange", mibService.findName("1.3.6.1.2.1.2.2.1.9"));
        assertEquals("IF-MIB::ifLastChange", mibService.findName("1.3.6.1.2.1.2.2.1.9"));
        assertEquals("IF-MIB::ifMIBObjects", mibService.findName("1.3.6.1.2.1.31.1"));
        assertEquals("RFC1065-SMI::internet", mibService.findName("1.3.6.1"));
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
        assertEquals("RFC1213-MIB::sysUpTime", variable.getFullName());
        Assertions.assertThat(variable.getDescription()).contains("in hundredths of a second");
    }

}