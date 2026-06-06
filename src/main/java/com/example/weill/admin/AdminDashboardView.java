package com.example.weill.admin;

import java.util.List;

public record AdminDashboardView(
    List<AdminUserSummary> users,
    List<AdminEvaluationSummary> evaluations,
    long completedEvaluations,
    double averageScore
) {
}
