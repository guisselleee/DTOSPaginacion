package com.example.weill.web;

import com.example.weill.audit.AuditEventType;
import com.example.weill.audit.AuditService;
import com.example.weill.attempt.AttemptResultView;
import com.example.weill.attempt.AttemptService;
import com.example.weill.attempt.WeillTestAttempt;
import com.example.weill.norms.PsychometricResult;
import com.example.weill.norms.WeillPsychometricEngine;
import com.example.weill.test.WeillTestCatalog;
import com.example.weill.user.WeillUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AttemptPageController {

    private final AttemptService attemptService;
    private final WeillTestCatalog catalog;
    private final SessionUserService sessionUserService;
    private final WeillPsychometricEngine psychometricEngine;
    private final AuditService auditService;

    public AttemptPageController(
        AttemptService attemptService,
        WeillTestCatalog catalog,
        SessionUserService sessionUserService,
        WeillPsychometricEngine psychometricEngine,
        AuditService auditService
    ) {
        this.attemptService = attemptService;
        this.catalog = catalog;
        this.sessionUserService = sessionUserService;
        this.psychometricEngine = psychometricEngine;
        this.auditService = auditService;
    }

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        WeillUser user = currentUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        if (!user.isEvaluatedUser()) {
            return redirectForRole(user);
        }
        model.addAttribute("user", user);
        return "index";
    }

    @PostMapping("/attempts")
    public String createAttempt(HttpServletRequest request, HttpSession session) {
        WeillUser user = currentUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        if (!user.isEvaluatedUser()) {
            return redirectForRole(user);
        }
        WeillTestAttempt attempt = attemptService.startAttempt(user);
        auditService.record(
            AuditEventType.EVALUATION_STARTED,
            user,
            attempt.getId(),
            request,
            "Evaluación iniciada."
        );
        return "redirect:/attempts/" + attempt.getId() + "/test";
    }

    @GetMapping("/attempts/{attemptId}/test")
    public String test(@PathVariable long attemptId, HttpSession session, Model model) {
        WeillUser user = currentUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        if (!user.isEvaluatedUser()) {
            return redirectForRole(user);
        }
        model.addAttribute("attempt", attemptService.getAttemptForUser(attemptId, user.getId()));
        model.addAttribute("pages", catalog.pages());
        model.addAttribute("totalItems", catalog.totalItems());
        return "test";
    }

    @GetMapping("/attempts/{attemptId}/completed")
    public String completed(@PathVariable long attemptId, HttpSession session, Model model) {
        WeillUser user = currentRegularUser(session);
        if (user == null) {
            return redirectForCurrentSession(session);
        }
        WeillTestAttempt attempt = attemptService.getAttemptForUser(attemptId, user.getId());
        if (!attempt.isCompleted()) {
            return "redirect:/attempts/" + attemptId + "/test";
        }
        model.addAttribute("attempt", attempt);
        return "completed";
    }

    @GetMapping("/attempts/{attemptId}/results")
    public String results(
        @PathVariable long attemptId,
        HttpServletRequest request,
        HttpSession session,
        Model model
    ) {
        WeillUser user = currentRegularUser(session);
        if (user == null) {
            return redirectForCurrentSession(session);
        }
        AttemptResultView result = attemptService.result(attemptId, user.getId());
        if (!result.attempt().isCompleted()) {
            return "redirect:/attempts/" + attemptId + "/test";
        }
        PsychometricResult psychometricResult = psychometricEngine.calculate(
            user,
            result.attempt().getTotalScore(),
            result.attempt().getCompletedAt().toLocalDate()
        );
        model.addAttribute("result", result);
        model.addAttribute("psychometric", psychometricResult);
        model.addAttribute("maxScore", catalog.totalItems());
        auditService.record(
            AuditEventType.RESULT_VIEWED,
            user,
            attemptId,
            request,
            "El participante consultó sus resultados."
        );
        return "results";
    }

    private WeillUser currentUser(HttpSession session) {
        return sessionUserService.currentUser(session).orElse(null);
    }

    private WeillUser currentRegularUser(HttpSession session) {
        WeillUser user = currentUser(session);
        if (user == null || !user.isEvaluatedUser()) {
            return null;
        }
        return user;
    }

    private String redirectForCurrentSession(HttpSession session) {
        WeillUser user = currentUser(session);
        return user == null ? "redirect:/login" : redirectForRole(user);
    }

    private String redirectForRole(WeillUser user) {
        return user.canManageSystem() ? "redirect:/system" : "redirect:/admin";
    }
}
