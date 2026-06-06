package com.example.weill.audit;

import com.example.weill.user.UserRole;
import com.example.weill.user.WeillUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class AuditServiceTests {

    @Test
    void recordsActorAttemptSessionAndForwardedIp() {
        AtomicReference<AuditLog> savedLog = new AtomicReference<>();
        AuditLogRepository repository = repositorySavingInto(savedLog);
        AuditService service = new AuditService(repository);
        WeillUser admin = new WeillUser(
            "admin@weill.local",
            "Administrador",
            "hash",
            UserRole.ADMIN
        );
        setId(admin, 9L);
        HttpSession session = session("session-123");
        HttpServletRequest request = request(session, "203.0.113.10, 10.0.0.1", "127.0.0.1");

        service.record(
            AuditEventType.ADMIN_ACCESS,
            admin,
            42L,
            request,
            "Consulta administrativa."
        );

        AuditLog log = savedLog.get();
        assertThat(log.getEventType()).isEqualTo(AuditEventType.ADMIN_ACCESS);
        assertThat(log.getActorUserId()).isEqualTo(9L);
        assertThat(log.getActorEmail()).isEqualTo("admin@weill.local");
        assertThat(log.getActorRole()).isEqualTo("ADMIN");
        assertThat(log.getAttemptId()).isEqualTo(42L);
        assertThat(log.getSessionId()).isEqualTo("session-123");
        assertThat(log.getIpAddress()).isEqualTo("203.0.113.10");
    }

    @Test
    void storesSessionTimeoutAsAnIndependentAuditEvent() {
        AtomicReference<AuditLog> savedLog = new AtomicReference<>();
        AuditService service = new AuditService(repositorySavingInto(savedLog));

        service.recordSessionTimeout(7L, "user@example.com", "USER", "expired-session");

        AuditLog log = savedLog.get();
        assertThat(log.getEventType()).isEqualTo(AuditEventType.SESSION_TIMEOUT);
        assertThat(log.getActorUserId()).isEqualTo(7L);
        assertThat(log.getSessionId()).isEqualTo("expired-session");
        assertThat(log.getDetails()).isEqualTo("Sesión cerrada por inactividad.");
    }

    private AuditLogRepository repositorySavingInto(AtomicReference<AuditLog> savedLog) {
        return (AuditLogRepository) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{AuditLogRepository.class},
            (proxy, method, arguments) -> {
                if (method.getName().equals("save")) {
                    savedLog.set((AuditLog) arguments[0]);
                    return arguments[0];
                }
                if (method.getName().equals("toString")) {
                    return "StubAuditLogRepository";
                }
                throw new UnsupportedOperationException(method.getName());
            }
        );
    }

    private HttpSession session(String id) {
        return (HttpSession) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{HttpSession.class},
            (proxy, method, arguments) -> {
                if (method.getName().equals("getId")) {
                    return id;
                }
                if (method.getName().equals("toString")) {
                    return "StubSession";
                }
                return null;
            }
        );
    }

    private HttpServletRequest request(
        HttpSession session,
        String forwardedFor,
        String remoteAddress
    ) {
        return (HttpServletRequest) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{HttpServletRequest.class},
            (proxy, method, arguments) -> switch (method.getName()) {
                case "getSession" -> session;
                case "getHeader" -> "X-Forwarded-For".equals(arguments[0]) ? forwardedFor : null;
                case "getRemoteAddr" -> remoteAddress;
                case "toString" -> "StubRequest";
                default -> null;
            }
        );
    }

    private void setId(WeillUser user, long id) {
        try {
            var field = WeillUser.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
