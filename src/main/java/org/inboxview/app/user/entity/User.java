package org.inboxview.app.user.entity;

import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "[user]")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class User {
    @Id
    @GeneratedValue
    @Column(name = "user_id", nullable = false)
    private Long id;

    @Column(name = "user_guid", nullable = false)
    private String guid;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "date_added", updatable = false, nullable = false)
    private OffsetDateTime dateAdded;

    @Column(name = "date_updated", insertable = false)
    private OffsetDateTime dateUpdated;

    @Column(name = "date_deleted", insertable = false)
    private OffsetDateTime dateDeleted;

    @Column(name = "date_verified")
    private OffsetDateTime dateVerified;

    @Version
    private int version;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_date_requested")
    private OffsetDateTime passwordResetDateRequested;

    @Column(name = "password_reset_count")
    private Long passwordResetCount;

    @Column(name = "password_date_reset")
    private OffsetDateTime passwordDateReset;

}
