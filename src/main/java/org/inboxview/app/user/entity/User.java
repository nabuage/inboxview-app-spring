package org.inboxview.app.user.entity;

import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "[user]")
@Getter
@Setter
@NoArgsConstructor
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
    @Temporal(TemporalType.TIMESTAMP)
    private OffsetDateTime dateAdded;

    @Column(name = "date_updated", insertable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private OffsetDateTime dateUpdated;

    @Column(name = "date_deleted", insertable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private OffsetDateTime dateDeleted;

    @Column(name = "date_verified")
    @Temporal(TemporalType.TIMESTAMP)
    private OffsetDateTime dateVerified;

    @Version
    private int version;

}
