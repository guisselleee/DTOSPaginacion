package com.example.weill.admin;

public record AdminEvaluationSummary(
    long id,
    String participantName,
    String email,
    String status,
    boolean completed,
    String startedAt,
    String completedAt,
    String elapsedTime,
    int totalScore,
    int maxScore
) {
}
