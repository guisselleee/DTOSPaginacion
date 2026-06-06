package com.example.weill.attempt;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WeillTestAttemptRepository extends JpaRepository<WeillTestAttempt, Long> {

    @Override
    @EntityGraph(attributePaths = "user")
    Optional<WeillTestAttempt> findById(Long id);

    @EntityGraph(attributePaths = "user")
    Optional<WeillTestAttempt> findByIdAndUser_Id(Long id, Long userId);

    @Query("""
        select attempt
        from WeillTestAttempt attempt
        join fetch attempt.user user
        where user.role = com.example.weill.user.UserRole.USER
          and (
            :query = ''
            or lower(user.displayName) like lower(concat('%', :query, '%'))
            or lower(user.email) like lower(concat('%', :query, '%'))
          )
        order by attempt.startedAt desc
        """)
    List<WeillTestAttempt> searchForAdmin(@Param("query") String query);
}
