package com.example.weill.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeillUserRepository extends JpaRepository<WeillUser, Long> {
    Optional<WeillUser> findByExternalUserId(String externalUserId);

    boolean existsByEmailIgnoreCase(String email);

    Optional<WeillUser> findByEmailIgnoreCase(String email);

    Optional<WeillUser> findByEmailIgnoreCaseAndRole(String email, UserRole role);
}
