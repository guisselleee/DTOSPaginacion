package com.example.weill.attempt;

public record CompleteAttemptResult(
    long attemptId,
    int totalScore,
    int maxScore,
    String confirmationUrl
) {
}
