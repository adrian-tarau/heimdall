package net.microfalx.heimdall.database.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("databaseUserRepository")
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {

    /**
     * Finds a user by its user-name.
     *
     * @param name the name of the user
     * @return an optional user
     */
    Optional<User> findByName(String name);
}
