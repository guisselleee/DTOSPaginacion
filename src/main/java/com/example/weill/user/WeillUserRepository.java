package com.example.weill.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WeillUserRepository extends JpaRepository<WeillUser, Long> {
    Optional<WeillUser> findByExternalUserId(String externalUserId);

    boolean existsByEmailIgnoreCase(String email);

    Optional<WeillUser> findByEmailIgnoreCase(String email);

    Optional<WeillUser> findByEmailIgnoreCaseAndRole(String email, UserRole role);

    List<WeillUser> findByRoleNotOrderByDisplayNameAsc(UserRole role);

    @Query("""
        select user
        from WeillUser user
        where user.role = com.example.weill.user.UserRole.EVALUADO
          and (
            :query = ''
            or lower(user.displayName) like lower(concat('%', :query, '%'))
            or lower(user.email) like lower(concat('%', :query, '%'))
          )
        order by user.displayName asc
        """)
    List<WeillUser> searchRegularUsers(@Param("query") String query);
}
