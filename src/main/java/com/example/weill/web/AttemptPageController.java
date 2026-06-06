package com.example.weill.web;

import com.example.weill.attempt.AttemptResultView;
import com.example.weill.attempt.AttemptService;
import com.example.weill.attempt.WeillTestAttempt;
import com.example.weill.test.WeillTestCatalog;
import com.example.weill.user.WeillUser;
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

    public AttemptPageController(
        AttemptService attemptService,
        WeillTestCatalog catalog,
        SessionUserService sessionUserService
    ) {
        this.attemptService = attemptService;
        this.catalog = catalog;
        this.sessionUserService = sessionUserService;
    }

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        WeillUser user = currentUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        if (user.isAdmin()) {
            return "redirect:/admin";
        }
        model.addAttribute("user", user);
        return "index";
    }

    @PostMapping("/attempts")
    public String createAttempt(HttpSession session) {
        WeillUser user = currentUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        if (user.isAdmin()) {
            return "redirect:/admin";
        }
        WeillTestAttempt attempt = attemptService.startAttempt(user);
        return "redirect:/attempts/" + attempt.getId() + "/test";
    }

    @GetMapping("/attempts/{attemptId}/test")
    public String test(@PathVariable long attemptId, HttpSession session, Model model) {
        WeillUser user = currentUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        if (user.isAdmin()) {
            return "redirect:/admin";
        }
        model.addAttribute("attempt", attemptService.getAttemptForUser(attemptId, user.getId()));
        model.addAttribute("pages", catalog.pages());
        model.addAttribute("totalItems", catalog.totalItems());
        return "test";
    }

    @GetMapping("/attempts/{attemptId}/results")
    public String results(@PathVariable long attemptId, HttpSession session, Model model) {
        WeillUser user = currentUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        if (user.isAdmin()) {
            return "redirect:/admin";
        }
        AttemptResultView result = attemptService.result(attemptId, user.getId());
        model.addAttribute("result", result);
        model.addAttribute("maxScore", catalog.totalItems());
        return "results";
    }

    private WeillUser currentUser(HttpSession session) {
        return sessionUserService.currentUser(session).orElse(null);
    }
}
