package com.example.weill.attempt;

import com.example.weill.test.DropZoneDetector;
import com.example.weill.test.WeillTestCatalog;
import com.example.weill.user.WeillUserRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttemptServiceAuthorizationTests {

    @Test
    void loadsAnAttemptUsingBothAttemptAndUserIds() {
        AtomicReference<Long> requestedAttemptId = new AtomicReference<>();
        AtomicReference<Long> requestedUserId = new AtomicReference<>();
        WeillTestAttemptRepository attemptRepository = proxy(
            WeillTestAttemptRepository.class,
            (methodName, arguments) -> {
                if (methodName.equals("findByIdAndUser_Id")) {
                    requestedAttemptId.set((Long) arguments[0]);
                    requestedUserId.set((Long) arguments[1]);
                    return Optional.empty();
                }
                throw new UnsupportedOperationException(methodName);
            }
        );

        AttemptService service = new AttemptService(
            unsupported(WeillUserRepository.class),
            attemptRepository,
            unsupported(ItemResponseRepository.class),
            new WeillTestCatalog(new DropZoneDetector())
        );

        assertThatThrownBy(() -> service.getAttemptForUser(25L, 7L))
            .isInstanceOf(AttemptNotFoundException.class);
        assertThat(requestedAttemptId).hasValue(25L);
        assertThat(requestedUserId).hasValue(7L);
    }

    private <T> T unsupported(Class<T> type) {
        return proxy(type, (methodName, arguments) -> {
            throw new UnsupportedOperationException(methodName);
        });
    }

    private <T> T proxy(Class<T> type, RepositoryCall call) {
        return type.cast(Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{type},
            (proxy, method, arguments) -> call.invoke(method.getName(), arguments == null ? new Object[0] : arguments)
        ));
    }

    @FunctionalInterface
    private interface RepositoryCall {
        Object invoke(String methodName, Object[] arguments);
    }
}
