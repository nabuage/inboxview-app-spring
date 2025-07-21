package org.inboxview.app.user.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@RequiredArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue
    @Column(name = "refresh_token_id", nullable = false)
    private Long refreshTokenId;

    @Column(name = "refresh_token_guid", nullable = false)
    private String guid;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "date_added", nullable = false, updatable = false)
    private OffsetDateTime dateAdded;

    @Column(name = "expiration_date")
    private OffsetDateTime expirationDate;
    
}
