package net.microfalx.heimdall.protocol.snmp.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SnmpEventRepository extends JpaRepository<SnmpEvent, Integer>, JpaSpecificationExecutor {
}
