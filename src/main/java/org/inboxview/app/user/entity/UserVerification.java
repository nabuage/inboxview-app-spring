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
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_verification")
@Getter
@Setter
@NoArgsConstructor
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
    @Temporal(TemporalType.TIMESTAMP)
    private OffsetDateTime dateVerified;

    @Column(name = "date_added")
    @Temporal(TemporalType.TIMESTAMP)
    private OffsetDateTime dateAdded;

    @Column(name = "date_deleted")
    @Temporal(TemporalType.TIMESTAMP)
    private OffsetDateTime dateDeleted;    
}
