package com.example.weill.web;

import com.example.weill.admin.AdminDashboardView;
import com.example.weill.admin.AdminEvaluationDetail;
import com.example.weill.admin.AdminEvaluationService;
import com.example.weill.audit.AuditEventType;
import com.example.weill.audit.AuditService;
import com.example.weill.user.InvalidUserFormException;
import com.example.weill.user.UserService;
import com.example.weill.user.WeillUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AdminController {

    private final UserService userService;
    private final SessionUserService sessionUserService;
    private final AdminEvaluationService adminEvaluationService;
    private final AuditService auditService;

    public AdminController(
        UserService userService,
        SessionUserService sessionUserService,
        AdminEvaluationService adminEvaluationService,
        AuditService auditService
    ) {
        this.userService = userService;
        this.sessionUserService = sessionUserService;
        this.adminEvaluationService = adminEvaluationService;
        this.auditService = auditService;
    }

    @GetMapping("/admin/login")
    public String login(HttpSession session, Model model) {
        Optional<WeillUser> currentUser = sessionUserService.currentUser(session);
        if (currentUser.isPresent() && !currentUser.get().isEvaluatedUser()) {
            return redirectForInstitutionalRole(currentUser.get());
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
            Optional<WeillUser> institutionalUser =
                userService.authenticateInstitutionalUser(email, password);
            if (institutionalUser.isEmpty()) {
                redirectAttributes.addFlashAttribute("email", email);
                redirectAttributes.addFlashAttribute("error", "Correo o contraseña institucional incorrectos.");
                return "redirect:/admin/login";
            }

            request.changeSessionId();
            session.setAttribute(AuthController.SESSION_USER_ID, institutionalUser.get().getId());
            auditService.bindActorToSession(session, institutionalUser.get());
            auditService.record(
                AuditEventType.LOGIN,
                institutionalUser.get(),
                null,
                request,
                "Inicio de sesión institucional como "
                    + institutionalUser.get().getRole().getDisplayName() + "."
            );
            return redirectForInstitutionalRole(institutionalUser.get());
        } catch (InvalidUserFormException exception) {
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/admin/login";
        }
    }

    @GetMapping("/admin")
    public String dashboard(
        @RequestParam(defaultValue = "") String query,
        HttpServletRequest request,
        HttpSession session,
        Model model
    ) {
        Optional<WeillUser> professional = currentClinicalProfessional(session);
        if (professional.isEmpty()) {
            return "redirect:/admin/login";
        }
        AdminDashboardView dashboard = adminEvaluationService.dashboard(query);
        model.addAttribute("admin", professional.get());
        model.addAttribute("query", query);
        model.addAttribute("dashboard", dashboard);
        auditService.record(
            AuditEventType.ADMIN_ACCESS,
            professional.get(),
            null,
            request,
            query.isBlank() ? "Acceso al panel administrativo." : "Búsqueda administrativa: " + query
        );
        return "admin-dashboard";
    }

    @GetMapping("/admin/evaluations/{attemptId}")
    public String evaluation(
        @PathVariable long attemptId,
        HttpServletRequest request,
        HttpSession session,
        Model model
    ) {
        Optional<WeillUser> professional = currentClinicalProfessional(session);
        if (professional.isEmpty()) {
            return "redirect:/admin/login";
        }
        AdminEvaluationDetail detail = adminEvaluationService.evaluation(attemptId);
        model.addAttribute("detail", detail);
        auditService.record(
            AuditEventType.ADMIN_ACCESS,
            professional.get(),
            attemptId,
            request,
            "Acceso administrativo al detalle de evaluación."
        );
        auditService.record(
            AuditEventType.RESULT_VIEWED,
            professional.get(),
            attemptId,
            request,
            "El administrador consultó resultados y respuestas."
        );
        return "admin-evaluation";
    }

    @PostMapping("/admin/logout")
    public String logout(HttpSession session) {
        auditService.markExplicitClose(session);
        session.invalidate();
        return "redirect:/admin/login";
    }

    private Optional<WeillUser> currentClinicalProfessional(HttpSession session) {
        return sessionUserService.currentUser(session).filter(WeillUser::canViewClinicalData);
    }

    private String redirectForInstitutionalRole(WeillUser user) {
        return user.canManageSystem() ? "redirect:/system" : "redirect:/admin";
    }
}
