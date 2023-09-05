package net.microfalx.heimdall.protocol.snmp.mib;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collection;

import static net.microfalx.resource.ClassPathResource.directory;
import static net.microfalx.resource.ClassPathResource.file;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MibParserTest {

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

}