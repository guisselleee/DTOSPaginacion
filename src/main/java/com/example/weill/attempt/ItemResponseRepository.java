package com.example.weill.attempt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ItemResponseRepository extends JpaRepository<ItemResponse, Long> {

    List<ItemResponse> findByAttemptIdOrderByPageNumberAscRowNumberAscItemNumberAsc(Long attemptId);

    Optional<ItemResponse> findByAttemptIdAndPageNumberAndRowNumberAndItemNumber(
        Long attemptId,
        short pageNumber,
        short rowNumber,
        short itemNumber
    );

    long countByAttemptId(Long attemptId);

    @Query("select coalesce(sum(response.score), 0) from ItemResponse response where response.attempt.id = :attemptId")
    int sumScoreByAttemptId(Long attemptId);
}
