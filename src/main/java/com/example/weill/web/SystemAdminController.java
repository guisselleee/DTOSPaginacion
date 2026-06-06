package com.example.weill.web;

import com.example.weill.audit.AuditEventType;
import com.example.weill.audit.AuditService;
import com.example.weill.system.SystemAdministrationService;
import com.example.weill.user.InvalidUserFormException;
import com.example.weill.user.UserRole;
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

import java.util.List;
import java.util.Optional;

@Controller
public class SystemAdminController {

    private static final List<UserRole> AUTHORIZED_ROLES = List.of(
        UserRole.LICENCIADO,
        UserRole.DECANA,
        UserRole.ADMIN_TECNICO
    );

    private final SessionUserService sessionUserService;
    private final SystemAdministrationService systemAdministrationService;
    private final UserService userService;
    private final AuditService auditService;

    public SystemAdminController(
        SessionUserService sessionUserService,
        SystemAdministrationService systemAdministrationService,
        UserService userService,
        AuditService auditService
    ) {
        this.sessionUserService = sessionUserService;
        this.systemAdministrationService = systemAdministrationService;
        this.userService = userService;
        this.auditService = auditService;
    }

    @GetMapping("/system")
    public String dashboard(HttpServletRequest request, HttpSession session, Model model) {
        Optional<WeillUser> technicalAdmin = currentTechnicalAdmin(session);
        if (technicalAdmin.isEmpty()) {
            return "redirect:/admin/login";
        }
        model.addAttribute("technicalAdmin", technicalAdmin.get());
        model.addAttribute("authorizedUsers", systemAdministrationService.authorizedUsers());
        model.addAttribute("auditEvents", systemAdministrationService.recentAuditEvents());
        model.addAttribute("roles", AUTHORIZED_ROLES);
        auditService.record(
            AuditEventType.ADMIN_ACCESS,
            technicalAdmin.get(),
            null,
            request,
            "Acceso al panel de administración técnica."
        );
        return "system-dashboard";
    }

    @PostMapping("/system/users")
    public String saveAuthorizedUser(
        @RequestParam String name,
        @RequestParam String email,
        @RequestParam(required = false) String password,
        @RequestParam UserRole role,
        HttpServletRequest request,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        Optional<WeillUser> technicalAdmin = currentTechnicalAdmin(session);
        if (technicalAdmin.isEmpty()) {
            return "redirect:/admin/login";
        }
        try {
            WeillUser saved = userService.saveAuthorizedUser(name, email, password, role);
            auditService.record(
                AuditEventType.ADMIN_ACCESS,
                technicalAdmin.get(),
                null,
                request,
                "Usuario autorizado gestionado: " + saved.getEmail()
                    + " (" + saved.getRole().getDisplayName() + ")."
            );
            redirectAttributes.addFlashAttribute("message", "Usuario autorizado guardado.");
        } catch (InvalidUserFormException | IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/system";
    }

    private Optional<WeillUser> currentTechnicalAdmin(HttpSession session) {
        return sessionUserService.currentUser(session).filter(WeillUser::canManageSystem);
    }
}
