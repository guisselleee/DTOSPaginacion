package com.example.weill.audit;

import com.example.weill.web.AuthController;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class SessionAuditListener implements HttpSessionListener {

    private final Supplier<AuditService> auditService;

    @Autowired
    public SessionAuditListener(ObjectProvider<AuditService> auditService) {
        this(auditService::getObject);
    }

    SessionAuditListener(AuditService auditService) {
        this(() -> auditService);
    }

    private SessionAuditListener(Supplier<AuditService> auditService) {
        this.auditService = auditService;
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        Object userId = session.getAttribute(AuthController.SESSION_USER_ID);
        boolean explicitlyClosed = Boolean.TRUE.equals(session.getAttribute(AuditService.SESSION_EXPLICIT_CLOSE));
        if (!(userId instanceof Long actorUserId) || explicitlyClosed || !expiredByInactivity(session)) {
            return;
        }
        auditService.get().recordSessionTimeout(
            actorUserId,
            stringAttribute(session, AuditService.SESSION_ACTOR_EMAIL),
            stringAttribute(session, AuditService.SESSION_ACTOR_ROLE),
            session.getId()
        );
    }

    private boolean expiredByInactivity(HttpSession session) {
        int timeoutSeconds = session.getMaxInactiveInterval();
        if (timeoutSeconds <= 0) {
            return false;
        }
        long inactiveMillis = System.currentTimeMillis() - session.getLastAccessedTime();
        return inactiveMillis >= timeoutSeconds * 1000L;
    }

    private String stringAttribute(HttpSession session, String name) {
        Object value = session.getAttribute(name);
        return value instanceof String text ? text : null;
    }
}
