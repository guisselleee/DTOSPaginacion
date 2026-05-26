package com.example.weill.web;

import com.example.weill.attempt.AttemptService;
import com.example.weill.attempt.CompleteAttemptResult;
import com.example.weill.attempt.SaveResponsesRequest;
import com.example.weill.attempt.SaveResponsesResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/attempts")
public class AttemptApiController {

    private final AttemptService attemptService;

    public AttemptApiController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @PostMapping("/{attemptId}/responses")
    public ResponseEntity<SaveResponsesResult> saveResponses(
        @PathVariable long attemptId,
        @RequestBody SaveResponsesRequest request
    ) {
        return ResponseEntity.ok(attemptService.saveResponses(attemptId, responsesFrom(request)));
    }

    @PostMapping("/{attemptId}/complete")
    public ResponseEntity<CompleteAttemptResult> completeAttempt(
        @PathVariable long attemptId,
        @RequestBody SaveResponsesRequest request
    ) {
        return ResponseEntity.ok(attemptService.completeAttempt(attemptId, responsesFrom(request)));
    }

    private List<com.example.weill.attempt.ResponseSubmission> responsesFrom(SaveResponsesRequest request) {
        if (request == null || request.responses() == null) {
            return List.of();
        }
        return request.responses();
    }
}
