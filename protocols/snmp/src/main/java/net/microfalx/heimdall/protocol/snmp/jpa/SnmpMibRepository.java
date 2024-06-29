package net.microfalx.heimdall.protocol.snmp.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SnmpMibRepository extends JpaRepository<SnmpMib, Integer>, JpaSpecificationExecutor<SnmpMib> {

    /**
     * Finds the Mib module reference by its module identifier.
     *
     * @param moduleId the Mib module identifier
     * @return the SNMP Mib or null otherwise
     */
    SnmpMib findByModuleId(String moduleId);
}
