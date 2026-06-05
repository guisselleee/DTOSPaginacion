package com.example.weill.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "weill_users")
public class WeillUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_user_id", unique = true)
    private String externalUserId;

    @Column(unique = true)
    private String email;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "education_level")
    private String educationLevel;

    @Column(name = "school_center")
    private String schoolCenter;

    private String municipality;

    private String department;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "currently_studying")
    private Boolean currentlyStudying;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected WeillUser() {
    }

    public WeillUser(String externalUserId, String displayName) {
        this.externalUserId = externalUserId;
        this.displayName = displayName;
    }

    public WeillUser(
        String email,
        String displayName,
        String educationLevel,
        String schoolCenter,
        String municipality,
        String department,
        LocalDate birthDate,
        boolean currentlyStudying
    ) {
        this.email = email;
        this.displayName = displayName;
        this.educationLevel = educationLevel;
        this.schoolCenter = schoolCenter;
        this.municipality = municipality;
        this.department = department;
        this.birthDate = birthDate;
        this.currentlyStudying = currentlyStudying;
    }

    public Long getId() {
        return id;
    }

    public String getExternalUserId() {
        return externalUserId;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEducationLevel() {
        return educationLevel;
    }

    public String getSchoolCenter() {
        return schoolCenter;
    }

    public String getMunicipality() {
        return municipality;
    }

    public String getDepartment() {
        return department;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public Boolean getCurrentlyStudying() {
        return currentlyStudying;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void updateDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
