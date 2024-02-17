package net.microfalx.heimdall.protocol.core.jpa;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface AddressRepository extends JpaRepository<Address, Integer>, JpaSpecificationExecutor<Address> {

    /**
     * Returns the address by value.
     *
     * @param value the address value
     * @return the address if exists, otherwise returns null
     */
    Address findByValue(String value);
}
