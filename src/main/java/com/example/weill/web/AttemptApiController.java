package com.example.weill.web;

import com.example.weill.audit.AuditEventType;
import com.example.weill.audit.AuditService;
import com.example.weill.attempt.AttemptService;
import com.example.weill.attempt.CompleteAttemptResult;
import com.example.weill.attempt.SaveResponsesRequest;
import com.example.weill.attempt.SaveResponsesResult;
import com.example.weill.user.WeillUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/attempts")
public class AttemptApiController {

    private final AttemptService attemptService;
    private final SessionUserService sessionUserService;
    private final AuditService auditService;

    public AttemptApiController(
        AttemptService attemptService,
        SessionUserService sessionUserService,
        AuditService auditService
    ) {
        this.attemptService = attemptService;
        this.sessionUserService = sessionUserService;
        this.auditService = auditService;
    }

    @PostMapping("/{attemptId}/responses")
    public ResponseEntity<SaveResponsesResult> saveResponses(
        @PathVariable long attemptId,
        HttpSession session,
        @RequestBody SaveResponsesRequest request
    ) {
        WeillUser user = requireRegularUser(session);
        return ResponseEntity.ok(attemptService.saveResponses(attemptId, user.getId(), responsesFrom(request)));
    }

    @PostMapping("/{attemptId}/complete")
    public ResponseEntity<CompleteAttemptResult> completeAttempt(
        @PathVariable long attemptId,
        HttpServletRequest servletRequest,
        HttpSession session,
        @RequestBody SaveResponsesRequest request
    ) {
        WeillUser user = requireRegularUser(session);
        CompleteAttemptResult result =
            attemptService.completeAttempt(attemptId, user.getId(), responsesFrom(request));
        auditService.record(
            AuditEventType.EVALUATION_COMPLETED,
            user,
            attemptId,
            servletRequest,
            "Evaluación finalizada con " + result.totalScore() + "/" + result.maxScore() + " aciertos."
        );
        return ResponseEntity.ok(result);
    }

    private List<com.example.weill.attempt.ResponseSubmission> responsesFrom(SaveResponsesRequest request) {
        if (request == null || request.responses() == null) {
            return List.of();
        }
        return request.responses();
    }

    private WeillUser requireRegularUser(HttpSession session) {
        WeillUser user = sessionUserService.currentUser(session)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (!user.isEvaluatedUser()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return user;
    }
}
