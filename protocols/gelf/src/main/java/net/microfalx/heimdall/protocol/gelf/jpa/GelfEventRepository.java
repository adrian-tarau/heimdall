package net.microfalx.heimdall.protocol.gelf.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GelfEventRepository extends JpaRepository<GelfEvent,Long>, JpaSpecificationExecutor {
}
