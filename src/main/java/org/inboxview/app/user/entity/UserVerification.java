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
@Table(name = "user_verification")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserVerification {
    @Id
    @GeneratedValue
    @Column(name = "verification_id", nullable = false)
    private Long verificationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "attempt_count")
    private Long attemptCount;

    @Column(name = "date_verified")
    private OffsetDateTime dateVerified;

    @Column(name = "date_added")
    private OffsetDateTime dateAdded;

    @Column(name = "date_deleted")
    private OffsetDateTime dateDeleted;    
}
