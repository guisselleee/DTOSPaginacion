package com.example.weill.admin;

import com.example.weill.attempt.ItemResponseRepository;
import com.example.weill.attempt.WeillTestAttemptRepository;
import com.example.weill.norms.WeillPsychometricEngine;
import com.example.weill.user.WeillUserRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class AdminEvaluationServiceTests {

    @Test
    void formatsElapsedTimeUsingHoursMinutesAndSeconds() {
        OffsetDateTime start = OffsetDateTime.parse("2026-06-06T08:00:00-06:00");
        OffsetDateTime end = OffsetDateTime.parse("2026-06-06T09:02:03-06:00");

        assertThat(AdminEvaluationService.formatDuration(start, end)).isEqualTo("01:02:03");
        assertThat(AdminEvaluationService.formatDuration(null, end)).isEqualTo("No disponible");
    }

    @Test
    void trimsTheSearchTermForUserAndEvaluationQueries() {
        AtomicReference<String> userQuery = new AtomicReference<>();
        AtomicReference<String> attemptQuery = new AtomicReference<>();
        WeillUserRepository userRepository = proxy(WeillUserRepository.class, (method, arguments) -> {
            if (method.equals("searchRegularUsers")) {
                userQuery.set((String) arguments[0]);
                return List.of();
            }
            throw new UnsupportedOperationException(method);
        });
        WeillTestAttemptRepository attemptRepository = proxy(
            WeillTestAttemptRepository.class,
            (method, arguments) -> {
                if (method.equals("searchForAdmin")) {
                    attemptQuery.set((String) arguments[0]);
                    return List.of();
                }
                throw new UnsupportedOperationException(method);
            }
        );
        AdminEvaluationService service = new AdminEvaluationService(
            userRepository,
            attemptRepository,
            unsupported(ItemResponseRepository.class),
            unsupported(WeillPsychometricEngine.class)
        );

        AdminDashboardView dashboard = service.dashboard("  estudiante@example.com  ");

        assertThat(userQuery).hasValue("estudiante@example.com");
        assertThat(attemptQuery).hasValue("estudiante@example.com");
        assertThat(dashboard.users()).isEmpty();
        assertThat(dashboard.evaluations()).isEmpty();
    }

    private <T> T unsupported(Class<T> type) {
        if (type.isInterface()) {
            return proxy(type, (method, arguments) -> {
                throw new UnsupportedOperationException(method);
            });
        }
        return null;
    }

    private <T> T proxy(Class<T> type, RepositoryCall call) {
        return type.cast(Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{type},
            (proxy, method, arguments) -> {
                if (method.getName().equals("toString")) {
                    return "Stub" + type.getSimpleName();
                }
                return call.invoke(method.getName(), arguments == null ? new Object[0] : arguments);
            }
        ));
    }

    @FunctionalInterface
    private interface RepositoryCall {
        Object invoke(String methodName, Object[] arguments);
    }
}
