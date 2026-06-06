package com.example.weill.audit;

import com.example.weill.user.WeillUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    public static final String SESSION_ACTOR_EMAIL = "auditActorEmail";
    public static final String SESSION_ACTOR_ROLE = "auditActorRole";
    public static final String SESSION_EXPLICIT_CLOSE = "auditExplicitClose";

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(
        AuditEventType eventType,
        WeillUser actor,
        Long attemptId,
        HttpServletRequest request,
        String details
    ) {
        HttpSession session = request.getSession(false);
        repository.save(new AuditLog(
            eventType,
            actor == null ? null : actor.getId(),
            actor == null ? null : actor.getEmail(),
            actor == null ? null : actor.getRole().name(),
            attemptId,
            session == null ? null : session.getId(),
            clientIp(request),
            normalizeDetails(details)
        ));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSessionTimeout(
        Long actorUserId,
        String actorEmail,
        String actorRole,
        String sessionId
    ) {
        repository.save(new AuditLog(
            AuditEventType.SESSION_TIMEOUT,
            actorUserId,
            actorEmail,
            actorRole,
            null,
            sessionId,
            null,
            "Sesión cerrada por inactividad."
        ));
    }

    public void bindActorToSession(HttpSession session, WeillUser actor) {
        session.setAttribute(SESSION_ACTOR_EMAIL, actor.getEmail());
        session.setAttribute(SESSION_ACTOR_ROLE, actor.getRole().name());
        session.removeAttribute(SESSION_EXPLICIT_CLOSE);
    }

    public void markExplicitClose(HttpSession session) {
        session.setAttribute(SESSION_EXPLICIT_CLOSE, Boolean.TRUE);
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",", 2)[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String normalizeDetails(String details) {
        if (details == null || details.isBlank()) {
            return null;
        }
        String normalized = details.trim();
        return normalized.length() <= 500 ? normalized : normalized.substring(0, 500);
    }
}
