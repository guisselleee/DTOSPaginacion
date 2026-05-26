package com.example.weill.web;

import com.example.weill.attempt.AttemptResultView;
import com.example.weill.attempt.AttemptService;
import com.example.weill.attempt.WeillTestAttempt;
import com.example.weill.test.WeillTestCatalog;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AttemptPageController {

    private final AttemptService attemptService;
    private final WeillTestCatalog catalog;

    public AttemptPageController(AttemptService attemptService, WeillTestCatalog catalog) {
        this.attemptService = attemptService;
        this.catalog = catalog;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/attempts")
    public String createAttempt(
        @RequestParam String displayName,
        @RequestParam(required = false) String externalUserId
    ) {
        WeillTestAttempt attempt = attemptService.startAttempt(displayName, externalUserId);
        return "redirect:/attempts/" + attempt.getId() + "/test";
    }

    @GetMapping("/attempts/{attemptId}/test")
    public String test(@PathVariable long attemptId, Model model) {
        model.addAttribute("attempt", attemptService.getAttempt(attemptId));
        model.addAttribute("pages", catalog.pages());
        model.addAttribute("totalItems", catalog.totalItems());
        return "test";
    }

    @GetMapping("/attempts/{attemptId}/results")
    public String results(@PathVariable long attemptId, Model model) {
        AttemptResultView result = attemptService.result(attemptId);
        model.addAttribute("result", result);
        model.addAttribute("maxScore", catalog.totalItems());
        return "results";
    }
}
