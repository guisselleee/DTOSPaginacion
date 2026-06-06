package com.example.weill.web;

import com.example.weill.user.InvalidUserFormException;
import com.example.weill.user.UserService;
import com.example.weill.user.WeillUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AdminController {

    private final UserService userService;
    private final SessionUserService sessionUserService;

    public AdminController(UserService userService, SessionUserService sessionUserService) {
        this.userService = userService;
        this.sessionUserService = sessionUserService;
    }

    @GetMapping("/admin/login")
    public String login(HttpSession session, Model model) {
        Optional<WeillUser> currentUser = sessionUserService.currentUser(session);
        if (currentUser.filter(WeillUser::isAdmin).isPresent()) {
            return "redirect:/admin";
        }
        if (!model.containsAttribute("email")) {
            model.addAttribute("email", "");
        }
        return "admin-login";
    }

    @PostMapping("/admin/login")
    public String authenticate(
        @RequestParam String email,
        @RequestParam String password,
        HttpServletRequest request,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        try {
            Optional<WeillUser> admin = userService.authenticateAdmin(email, password);
            if (admin.isEmpty()) {
                redirectAttributes.addFlashAttribute("email", email);
                redirectAttributes.addFlashAttribute("error", "Correo o contraseña de administrador incorrectos.");
                return "redirect:/admin/login";
            }

            request.changeSessionId();
            session.setAttribute(AuthController.SESSION_USER_ID, admin.get().getId());
            return "redirect:/admin";
        } catch (InvalidUserFormException exception) {
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/admin/login";
        }
    }

    @GetMapping("/admin")
    public String dashboard(HttpSession session, Model model) {
        Optional<WeillUser> admin = sessionUserService.currentUser(session).filter(WeillUser::isAdmin);
        if (admin.isEmpty()) {
            return "redirect:/admin/login";
        }
        model.addAttribute("admin", admin.get());
        return "admin-dashboard";
    }

    @PostMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }
}
