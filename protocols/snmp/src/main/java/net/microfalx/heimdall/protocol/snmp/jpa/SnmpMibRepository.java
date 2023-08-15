package net.microfalx.heimdall.protocol.snmp.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SnmpMibRepository extends JpaRepository<SnmpMib, Integer> {
}
