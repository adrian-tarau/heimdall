package net.microfalx.heimdall.rest.core.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RestScheduleRepository extends JpaRepository<RestSchedule,Integer>, JpaSpecificationExecutor<RestSchedule> {
}
