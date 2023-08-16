package net.microfalx.heimdall.protocol.snmp.jpa;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Transactional
@Repository
public interface SnmpMibRepository extends JpaRepository<SnmpMib, Integer> {

    /**
     * Finds the Mib module reference by its module identifier.
     *
     * @param moduleId the Mib module identifier
     * @return the SNMP Mib or null otherwise
     */
    SnmpMib findByModuleId(String moduleId);
}
