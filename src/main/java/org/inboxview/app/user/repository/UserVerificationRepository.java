package org.inboxview.app.user.repository;

import java.util.Optional;

import org.inboxview.app.user.entity.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserVerificationRepository extends JpaRepository<UserVerification, Long> {
    @Query("SELECT uv FROM UserVerification uv WHERE uv.userId = ?1 AND uv.dateDeleted IS NULL ORDER BY uv.dateAdded DESC LIMIT 1")
    Optional<UserVerification> findByUserId(Long userId);
}