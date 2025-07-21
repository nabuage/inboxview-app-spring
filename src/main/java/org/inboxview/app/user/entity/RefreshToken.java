package org.inboxview.app.user.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_token")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
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
