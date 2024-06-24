package net.microfalx.heimdall.infrastructure.ping;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
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

    /**
     * Returns whether a ping is registered for a service and a service.
     *
     * @param serverId  the server identifier
     * @param serviceId the service identifier
     * @return {@code true} if a ping is registered, {@code false} otherwise
     */
    @Query(value = """
            SELECT count(*) ping_count FROM infrastructure_ping ip
            left join infrastructure_server iserver on ip.server_id = iserver.id
            left join infrastructure_service iservice on ip.service_id = iservice.id
            WHERE iserver.natural_id = ?1 and iservice.natural_id = ?2""",
            nativeQuery = true)
    int countPings(String serverId, String serviceId);
}
