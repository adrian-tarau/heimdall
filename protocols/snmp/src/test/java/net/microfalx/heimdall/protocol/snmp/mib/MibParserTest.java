package net.microfalx.heimdall.protocol.snmp.mib;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static net.microfalx.resource.ClassPathResource.directory;
import static net.microfalx.resource.ClassPathResource.file;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MibParserTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MibService.class);

    @Test
    void parseSnmpMib() throws IOException {
        MibParser mibParser = new MibParser(file("mib/base/SNMPv2-MIB"));
        MibModule module = mibParser.parse();
        assertEquals("SNMPv2-MIB", module.getIdToken());
        assertEquals("SNMPv2-MIB", module.getFileName());
        assertEquals(MibType.SYSTEM, module.getType());
        assertEquals(251, mibParser.getProblems().size());
        assertEquals(1, mibParser.getModules().size());
    }

    @Test
    void parseSnmpMibWithCore() throws IOException {
        MibParser mibParser = new MibParser(file("mib/base/SNMPv2-MIB"), MibType.IMPORT).add(directory("mib/base").list());
        MibModule module = mibParser.parse();
        assertEquals("SNMPv2-MIB", module.getIdToken());
        assertEquals(MibType.IMPORT, module.getType());
        assertEquals(6, mibParser.getProblems().size());
        assertEquals(23, mibParser.getModules().size());
    }

    @Test
    void parseSnmpCore() throws IOException {
        MibParser mibParser = new MibParser().add(directory("mib/base").list());
        Collection<MibModule> mibModules = mibParser.parseAll();
        assertEquals(23, mibModules.size());
        assertEquals(23, mibParser.getModules().size());
        assertEquals(6, mibParser.getProblems().size());
        Assertions.assertThat(mibParser.getProblemsReport()).contains("expecting \"APPLICATION\"").contains("Cannot find symbol");
    }

    @Test
    void parseSimulatorMibs() throws IOException {
        MibParser mibParser = new MibParser().add(directory("simulator/snmp/mib").list());
        Collection<MibModule> modules = mibParser.parseAll();
        assertTrue(modules.size() >= 16);
        for (MibModule module : modules) {
            printVariables(module);
            MibMetadataExtractor metadataExtractor = new MibMetadataExtractor(module);
            metadataExtractor.execute();
        }
    }

    @Test
    void parseSimulatorMibsWithCore() throws IOException {
        MibParser mibParser = new MibParser()
                .add(directory("simulator/snmp/mib").list())
                .add(directory("mib/base").list());
        Collection<MibModule> modules = mibParser.parseAll();
        assertTrue(modules.size() >= 30);
        for (MibModule module : modules) {
            printVariables(module);
            MibMetadataExtractor metadataExtractor = new MibMetadataExtractor(module);
            metadataExtractor.execute();
        }
    }

    private void printVariables(MibModule module) {
        LOGGER.info("Variables for " + module.getName());
        List<MibVariable> variables = new ArrayList<>(module.getVariables());
        variables.sort(Comparator.comparing(MibVariable::getName));
        for (MibVariable variable : variables) {
            LOGGER.info("  - " + variable.getName() + "\t\t\t" + variable.getOid() + "\t\t\t" + variable.getType()
                    + "\t\t\t" + variable.getUnits() + "\t\t\t" + variable.getModule().getName());
        }
    }

}