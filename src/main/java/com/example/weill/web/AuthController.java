package com.example.weill.web;

import com.example.weill.audit.AuditEventType;
import com.example.weill.audit.AuditService;
import com.example.weill.user.DuplicateEmailException;
import com.example.weill.user.InvalidUserFormException;
import com.example.weill.user.UserRegistrationForm;
import com.example.weill.user.UserService;
import com.example.weill.user.WeillUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    public static final String SESSION_USER_ID = "userId";

    private final UserService userService;
    private final AuditService auditService;

    public AuthController(UserService userService, AuditService auditService) {
        this.userService = userService;
        this.auditService = auditService;
    }

    @GetMapping("/login")
    public String login(Model model) {
        if (!model.containsAttribute("email")) {
            model.addAttribute("email", "");
        }
        return "login";
    }

    @PostMapping("/login")
    public String authenticate(
        @RequestParam String email,
        HttpServletRequest request,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        try {
            Optional<WeillUser> user = userService.findByEmail(email);
            if (user.isEmpty()) {
                redirectAttributes.addFlashAttribute("email", email);
                redirectAttributes.addFlashAttribute("error", "No encontramos un usuario registrado con ese correo.");
                return "redirect:/login";
            }

            request.changeSessionId();
            session.setAttribute(SESSION_USER_ID, user.get().getId());
            auditService.bindActorToSession(session, user.get());
            auditService.record(
                AuditEventType.LOGIN,
                user.get(),
                null,
                request,
                "Inicio de sesión de usuario."
            );
            return "redirect:/";
        } catch (InvalidUserFormException exception) {
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String register(Model model) {
        if (!model.containsAttribute("registration")) {
            model.addAttribute("registration", emptyRegistrationForm());
        }
        return "register";
    }

    @PostMapping("/register")
    public String createUser(
        @ModelAttribute("registration") UserRegistrationForm registration,
        HttpServletRequest request,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        try {
            WeillUser user = userService.register(registration);
            request.changeSessionId();
            session.setAttribute(SESSION_USER_ID, user.getId());
            auditService.bindActorToSession(session, user);
            auditService.record(
                AuditEventType.LOGIN,
                user,
                null,
                request,
                "Inicio de sesión posterior al registro."
            );
            return "redirect:/";
        } catch (DuplicateEmailException exception) {
            redirectAttributes.addFlashAttribute("registration", registration);
            redirectAttributes.addFlashAttribute("error", "Ese correo ya esta registrado. Inicia sesion para continuar.");
            return "redirect:/register";
        } catch (InvalidUserFormException exception) {
            redirectAttributes.addFlashAttribute("registration", registration);
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/register";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        auditService.markExplicitClose(session);
        session.invalidate();
        return "redirect:/login";
    }

    private UserRegistrationForm emptyRegistrationForm() {
        return new UserRegistrationForm(null, null, null, null, null, null, null, null);
    }
}
