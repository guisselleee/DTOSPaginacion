package com.example.weill.admin;

import com.example.weill.attempt.AttemptNotFoundException;
import com.example.weill.attempt.ItemResponse;
import com.example.weill.attempt.ItemResponseRepository;
import com.example.weill.attempt.WeillTestAttempt;
import com.example.weill.attempt.WeillTestAttemptRepository;
import com.example.weill.norms.PsychometricResult;
import com.example.weill.norms.WeillPsychometricEngine;
import com.example.weill.user.WeillUser;
import com.example.weill.user.WeillUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AdminEvaluationService {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final WeillUserRepository userRepository;
    private final WeillTestAttemptRepository attemptRepository;
    private final ItemResponseRepository responseRepository;
    private final WeillPsychometricEngine psychometricEngine;

    public AdminEvaluationService(
        WeillUserRepository userRepository,
        WeillTestAttemptRepository attemptRepository,
        ItemResponseRepository responseRepository,
        WeillPsychometricEngine psychometricEngine
    ) {
        this.userRepository = userRepository;
        this.attemptRepository = attemptRepository;
        this.responseRepository = responseRepository;
        this.psychometricEngine = psychometricEngine;
    }

    @Transactional(readOnly = true)
    public AdminDashboardView dashboard(String searchTerm) {
        String query = searchTerm == null ? "" : searchTerm.trim();
        List<AdminUserSummary> users = userRepository.searchRegularUsers(query).stream()
            .map(this::userSummary)
            .toList();
        List<AdminEvaluationSummary> evaluations = attemptRepository.searchForAdmin(query).stream()
            .map(this::evaluationSummary)
            .toList();
        List<AdminEvaluationSummary> completed = evaluations.stream()
            .filter(AdminEvaluationSummary::completed)
            .toList();
        double averageScore = completed.stream()
            .mapToInt(AdminEvaluationSummary::totalScore)
            .average()
            .orElse(0);
        return new AdminDashboardView(users, evaluations, completed.size(), averageScore);
    }

    @Transactional(readOnly = true)
    public AdminEvaluationDetail evaluation(long attemptId) {
        WeillTestAttempt attempt = attemptRepository.findById(attemptId)
            .filter(candidate -> candidate.getUser().isEvaluatedUser())
            .orElseThrow(() -> new AttemptNotFoundException(attemptId));
        List<ItemResponse> responses =
            responseRepository.findByAttemptIdOrderByPageNumberAscRowNumberAscItemNumberAsc(attemptId);
        PsychometricResult psychometricResult = attempt.isCompleted()
            ? psychometricEngine.calculate(
                attempt.getUser(),
                attempt.getTotalScore(),
                attempt.getCompletedAt().toLocalDate()
            )
            : null;
        return new AdminEvaluationDetail(
            attempt,
            responses,
            psychometricResult,
            formatDateTime(attempt.getStartedAt()),
            formatDateTime(attempt.getCompletedAt()),
            formatDuration(attempt.getStartedAt(), attempt.getCompletedAt())
        );
    }

    private AdminUserSummary userSummary(WeillUser user) {
        return new AdminUserSummary(
            user.getId(),
            user.getDisplayName(),
            user.getEmail(),
            user.getEducationLevel(),
            user.getMunicipality(),
            user.getDepartment()
        );
    }

    private AdminEvaluationSummary evaluationSummary(WeillTestAttempt attempt) {
        return new AdminEvaluationSummary(
            attempt.getId(),
            attempt.getUser().getDisplayName(),
            attempt.getUser().getEmail(),
            attempt.isCompleted() ? "Completada" : "En curso",
            attempt.isCompleted(),
            formatDateTime(attempt.getStartedAt()),
            formatDateTime(attempt.getCompletedAt()),
            formatDuration(attempt.getStartedAt(), attempt.getCompletedAt()),
            attempt.getTotalScore(),
            attempt.getMaxScore()
        );
    }

    private String formatDateTime(OffsetDateTime value) {
        return value == null ? "No disponible" : DATE_TIME_FORMAT.format(value);
    }

    static String formatDuration(OffsetDateTime startedAt, OffsetDateTime completedAt) {
        if (startedAt == null) {
            return "No disponible";
        }
        OffsetDateTime end = completedAt == null ? OffsetDateTime.now() : completedAt;
        long totalSeconds = Math.max(0, Duration.between(startedAt, end).toSeconds());
        long hours = totalSeconds / 3600;
        long minutes = totalSeconds % 3600 / 60;
        long seconds = totalSeconds % 60;
        return "%02d:%02d:%02d".formatted(hours, minutes, seconds);
    }
}
