package org.inboxview.app.user.repository;

import java.util.Optional;

import org.inboxview.app.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.username = ?1 AND u.dateDeleted IS NULL")
    Optional<User> findByUsername(String username);
    @Query("SELECT u FROM User u WHERE u.guid = ?1 AND u.dateDeleted IS NULL")
    Optional<User> findByGuid(String guid);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
