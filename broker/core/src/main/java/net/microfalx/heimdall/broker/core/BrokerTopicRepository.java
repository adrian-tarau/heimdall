package net.microfalx.heimdall.broker.core;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface BrokerTopicRepository extends JpaRepository<BrokerTopic, Integer>, JpaSpecificationExecutor<BrokerTopic> {

    /**
     * Returns all topics for a given broker.
     *
     * @return a non-null instance
     */
    List<BrokerTopic> findAllByBroker(Broker broker);

    /**
     * Finds topics by active field.
     *
     * @param active {@code true} to select active topics, {@code false} otherwise
     * @return a non-null instance
     */
    List<BrokerTopic> findByActive(boolean active);
}
