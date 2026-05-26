package com.example.weill.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "weill_users")
public class WeillUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_user_id", unique = true)
    private String externalUserId;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected WeillUser() {
    }

    public WeillUser(String externalUserId, String displayName) {
        this.externalUserId = externalUserId;
        this.displayName = displayName;
    }

    public Long getId() {
        return id;
    }

    public String getExternalUserId() {
        return externalUserId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void updateDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
