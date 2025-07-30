package org.inboxview.app.user.repository;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.inboxview.app.user.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByGuidAndAccessTokenAndExpirationDateAfter(String guid, String accessToken, OffsetDateTime expirationDate);

    void deleteByGuid(String guid);
}
