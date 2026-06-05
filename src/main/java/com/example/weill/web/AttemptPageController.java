package com.example.weill.web;

import com.example.weill.attempt.AttemptResultView;
import com.example.weill.attempt.AttemptService;
import com.example.weill.attempt.WeillTestAttempt;
import com.example.weill.test.WeillTestCatalog;
import com.example.weill.user.UserService;
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
    private final UserService userService;

    public AttemptPageController(AttemptService attemptService, WeillTestCatalog catalog, UserService userService) {
        this.attemptService = attemptService;
        this.catalog = catalog;
        this.userService = userService;
    }

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        WeillUser user = currentUser(session);
        if (user == null) {
            return "redirect:/login";
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
        WeillTestAttempt attempt = attemptService.startAttempt(user);
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

    private WeillUser currentUser(HttpSession session) {
        Object userId = session.getAttribute(AuthController.SESSION_USER_ID);
        if (userId instanceof Long id) {
            return userService.getUser(id);
        }
        return null;
    }
}
