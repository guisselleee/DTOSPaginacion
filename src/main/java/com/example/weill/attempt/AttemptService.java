package com.example.weill.attempt;

import com.example.weill.test.WeillTestCatalog;
import com.example.weill.user.WeillUser;
import com.example.weill.user.WeillUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AttemptService {

    private final WeillUserRepository userRepository;
    private final WeillTestAttemptRepository attemptRepository;
    private final ItemResponseRepository responseRepository;
    private final WeillTestCatalog catalog;

    public AttemptService(
        WeillUserRepository userRepository,
        WeillTestAttemptRepository attemptRepository,
        ItemResponseRepository responseRepository,
        WeillTestCatalog catalog
    ) {
        this.userRepository = userRepository;
        this.attemptRepository = attemptRepository;
        this.responseRepository = responseRepository;
        this.catalog = catalog;
    }

    @Transactional
    public WeillTestAttempt startAttempt(String displayName, String externalUserId) {
        String normalizedName = normalizeDisplayName(displayName);
        String normalizedExternalId = normalizeExternalId(externalUserId);
        WeillUser user = resolveUser(normalizedName, normalizedExternalId);
        return attemptRepository.save(new WeillTestAttempt(user));
    }

    @Transactional
    public WeillTestAttempt startAttempt(WeillUser user) {
        return attemptRepository.save(new WeillTestAttempt(user));
    }

    @Transactional(readOnly = true)
    public WeillTestAttempt getAttempt(long attemptId) {
        return attemptRepository.findById(attemptId)
            .orElseThrow(() -> new EntityNotFoundException("Attempt not found: " + attemptId));
    }

    @Transactional
    public SaveResponsesResult saveResponses(long attemptId, long userId, List<ResponseSubmission> submissions) {
        WeillTestAttempt attempt = getAttemptForUser(attemptId, userId);
        if (attempt.isCompleted()) {
            throw new IllegalStateException("Completed attempts cannot be changed.");
        }
        for (ResponseSubmission submission : submissions) {
            saveResponse(attempt, submission);
        }
        return currentProgress(attemptId);
    }

    @Transactional
    public CompleteAttemptResult completeAttempt(long attemptId, long userId, List<ResponseSubmission> submissions) {
        WeillTestAttempt attempt = getAttemptForUser(attemptId, userId);
        if (attempt.isCompleted()) {
            throw new IllegalStateException("Completed attempts cannot be changed.");
        }
        for (ResponseSubmission submission : submissions) {
            saveResponse(attempt, submission);
        }
        long answeredItems = responseRepository.countByAttemptId(attemptId);
        if (answeredItems < catalog.totalItems()) {
            throw new IllegalStateException("All 60 items must be answered before completing the attempt.");
        }
        int totalScore = responseRepository.sumScoreByAttemptId(attemptId);
        attempt.complete(totalScore);
        return new CompleteAttemptResult(attemptId, totalScore, catalog.totalItems(), "/attempts/" + attemptId + "/results");
    }

    @Transactional(readOnly = true)
    public AttemptResultView result(long attemptId, long userId) {
        WeillTestAttempt attempt = getAttemptForUser(attemptId, userId);
        List<ItemResponse> responses = responseRepository.findByAttemptIdOrderByPageNumberAscRowNumberAscItemNumberAsc(attemptId);
        return new AttemptResultView(attempt, responses, responses.size());
    }

    @Transactional(readOnly = true)
    public WeillTestAttempt getAttemptForUser(long attemptId, long userId) {
        return attemptRepository.findByIdAndUser_Id(attemptId, userId)
            .orElseThrow(() -> new AttemptNotFoundException(attemptId));
    }

    private void saveResponse(WeillTestAttempt attempt, ResponseSubmission submission) {
        int correctOption = catalog.correctOption(submission.pageNumber(), submission.rowNumber(), submission.itemNumber());
        ItemResponse response = responseRepository
            .findByAttemptIdAndPageNumberAndRowNumberAndItemNumber(
                attempt.getId(),
                (short) submission.pageNumber(),
                (short) submission.rowNumber(),
                (short) submission.itemNumber()
            )
            .orElseGet(() -> new ItemResponse(
                attempt,
                submission.pageNumber(),
                submission.rowNumber(),
                submission.itemNumber()
            ));
        response.answer(submission.selectedOption(), correctOption);
        responseRepository.save(response);
    }

    private SaveResponsesResult currentProgress(long attemptId) {
        long answeredItems = responseRepository.countByAttemptId(attemptId);
        int currentScore = responseRepository.sumScoreByAttemptId(attemptId);
        return new SaveResponsesResult(attemptId, answeredItems, catalog.totalItems(), currentScore, catalog.totalItems());
    }

    private WeillUser resolveUser(String displayName, String externalUserId) {
        if (externalUserId == null) {
            return userRepository.save(new WeillUser(null, displayName));
        }
        return userRepository.findByExternalUserId(externalUserId)
            .map(existing -> {
                existing.updateDisplayName(displayName);
                return existing;
            })
            .orElseGet(() -> userRepository.save(new WeillUser(externalUserId, displayName)));
    }

    private String normalizeDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return "Participante";
        }
        return displayName.trim();
    }

    private String normalizeExternalId(String externalUserId) {
        if (externalUserId == null || externalUserId.isBlank()) {
            return null;
        }
        return externalUserId.trim();
    }
}
