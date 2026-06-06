package com.example.weill.audit;

import com.example.weill.web.AuthController;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class SessionAuditListenerTests {

    @Test
    void recordsAuthenticatedSessionExpiration() {
        RecordingAuditService auditService = new RecordingAuditService();
        SessionAuditListener listener = new SessionAuditListener(auditService);
        Map<String, Object> attributes = authenticatedAttributes();

        listener.sessionDestroyed(new HttpSessionEvent(session(attributes)));

        assertThat(auditService.calls.get()).isEqualTo(1);
        assertThat(auditService.actorUserId.get()).isEqualTo(15L);
        assertThat(auditService.sessionId.get()).isEqualTo("session-timeout");
    }

    @Test
    void ignoresExplicitLogout() {
        RecordingAuditService auditService = new RecordingAuditService();
        SessionAuditListener listener = new SessionAuditListener(auditService);
        Map<String, Object> attributes = authenticatedAttributes();
        attributes.put(AuditService.SESSION_EXPLICIT_CLOSE, Boolean.TRUE);

        listener.sessionDestroyed(new HttpSessionEvent(session(attributes)));

        assertThat(auditService.calls.get()).isZero();
    }

    @Test
    void ignoresSessionDestructionBeforeTheInactivityLimit() {
        RecordingAuditService auditService = new RecordingAuditService();
        SessionAuditListener listener = new SessionAuditListener(auditService);

        listener.sessionDestroyed(new HttpSessionEvent(session(authenticatedAttributes(), false)));

        assertThat(auditService.calls.get()).isZero();
    }

    private Map<String, Object> authenticatedAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(AuthController.SESSION_USER_ID, 15L);
        attributes.put(AuditService.SESSION_ACTOR_EMAIL, "user@example.com");
        attributes.put(AuditService.SESSION_ACTOR_ROLE, "USER");
        return attributes;
    }

    private HttpSession session(Map<String, Object> attributes) {
        return session(attributes, true);
    }

    private HttpSession session(Map<String, Object> attributes, boolean expired) {
        return (HttpSession) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{HttpSession.class},
            (proxy, method, arguments) -> switch (method.getName()) {
                case "getAttribute" -> attributes.get((String) arguments[0]);
                case "getId" -> "session-timeout";
                case "getMaxInactiveInterval" -> 1800;
                case "getLastAccessedTime" -> expired
                    ? System.currentTimeMillis() - 1_801_000L
                    : System.currentTimeMillis();
                case "toString" -> "StubSession";
                default -> null;
            }
        );
    }

    private static final class RecordingAuditService extends AuditService {

        private final AtomicInteger calls = new AtomicInteger();
        private final AtomicReference<Long> actorUserId = new AtomicReference<>();
        private final AtomicReference<String> sessionId = new AtomicReference<>();

        private RecordingAuditService() {
            super(null);
        }

        @Override
        public void recordSessionTimeout(
            Long actorUserId,
            String actorEmail,
            String actorRole,
            String sessionId
        ) {
            calls.incrementAndGet();
            this.actorUserId.set(actorUserId);
            this.sessionId.set(sessionId);
        }
    }
}
