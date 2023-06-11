package net.microfalx.heimdall.protocol.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GelfEventRepository extends JpaRepository<GelfEvent,Long> {
}
