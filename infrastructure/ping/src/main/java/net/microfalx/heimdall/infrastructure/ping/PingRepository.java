package net.microfalx.heimdall.infrastructure.ping;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface PingRepository extends JpaRepository<Ping, Integer>, JpaSpecificationExecutor<Ping> {

    /**
     * Finds the registered pings using the active attribute.
     *
     * @param active {@code true} to retrive active pings, {@code false} otherwise
     * @return a non-null list
     */
    List<Ping> findByActive(boolean active);
}
