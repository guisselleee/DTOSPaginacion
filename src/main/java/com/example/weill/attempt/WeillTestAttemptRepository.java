package com.example.weill.attempt;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeillTestAttemptRepository extends JpaRepository<WeillTestAttempt, Long> {

    @Override
    @EntityGraph(attributePaths = "user")
    Optional<WeillTestAttempt> findById(Long id);

    @EntityGraph(attributePaths = "user")
    Optional<WeillTestAttempt> findByIdAndUser_Id(Long id, Long userId);
}
