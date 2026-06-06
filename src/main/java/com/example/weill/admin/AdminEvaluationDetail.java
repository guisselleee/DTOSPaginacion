package com.example.weill.admin;

import com.example.weill.attempt.ItemResponse;
import com.example.weill.attempt.WeillTestAttempt;
import com.example.weill.norms.PsychometricResult;

import java.util.List;

public record AdminEvaluationDetail(
    WeillTestAttempt attempt,
    List<ItemResponse> responses,
    PsychometricResult psychometricResult,
    String startedAt,
    String completedAt,
    String elapsedTime
) {
}
