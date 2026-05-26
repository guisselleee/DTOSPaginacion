package com.example.weill.attempt;

public record SaveResponsesResult(
    long attemptId,
    long answeredItems,
    int totalItems,
    int currentScore,
    int maxScore
) {
}
