package com.example.weill.attempt;

import com.example.weill.test.DropZoneDetector;
import com.example.weill.test.WeillTestCatalog;
import com.example.weill.user.WeillUser;
import com.example.weill.user.WeillUserRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class AttemptServiceSingleAttemptTests {

    @Test
    void returnsTheExistingAttemptInsteadOfCreatingAnotherOne() {
        WeillUser user = new WeillUser("external-7", "Evaluado");
        setId(user, 7L);
        WeillTestAttempt existingAttempt = new WeillTestAttempt(user);
        AtomicInteger saves = new AtomicInteger();
        WeillUserRepository userRepository = proxy(
            WeillUserRepository.class,
            (method, arguments) -> method.equals("findByIdForUpdate")
                ? Optional.of(user)
                : unsupported(method)
        );
        WeillTestAttemptRepository attemptRepository = proxy(
            WeillTestAttemptRepository.class,
            (method, arguments) -> {
                if (method.equals("findFirstByUser_IdOrderByStartedAtDesc")) {
                    return Optional.of(existingAttempt);
                }
                if (method.equals("save")) {
                    saves.incrementAndGet();
                    return arguments[0];
                }
                return unsupported(method);
            }
        );
        AttemptService service = new AttemptService(
            userRepository,
            attemptRepository,
            unsupportedRepository(ItemResponseRepository.class),
            new WeillTestCatalog(new DropZoneDetector())
        );

        assertThat(service.startAttempt(user)).isSameAs(existingAttempt);
        assertThat(saves).hasValue(0);
    }

    @Test
    void createsTheOnlyAttemptWhenNoneExists() {
        WeillUser user = new WeillUser("external-8", "Evaluado");
        setId(user, 8L);
        AtomicInteger saves = new AtomicInteger();
        WeillUserRepository userRepository = proxy(
            WeillUserRepository.class,
            (method, arguments) -> method.equals("findByIdForUpdate")
                ? Optional.of(user)
                : unsupported(method)
        );
        WeillTestAttemptRepository attemptRepository = proxy(
            WeillTestAttemptRepository.class,
            (method, arguments) -> {
                if (method.equals("findFirstByUser_IdOrderByStartedAtDesc")) {
                    return Optional.empty();
                }
                if (method.equals("save")) {
                    saves.incrementAndGet();
                    return arguments[0];
                }
                return unsupported(method);
            }
        );
        AttemptService service = new AttemptService(
            userRepository,
            attemptRepository,
            unsupportedRepository(ItemResponseRepository.class),
            new WeillTestCatalog(new DropZoneDetector())
        );

        assertThat(service.startAttempt(user).getUser()).isSameAs(user);
        assertThat(saves).hasValue(1);
    }

    private Object unsupported(String method) {
        throw new UnsupportedOperationException(method);
    }

    private <T> T unsupportedRepository(Class<T> type) {
        return proxy(type, (method, arguments) -> unsupported(method));
    }

    private <T> T proxy(Class<T> type, RepositoryCall call) {
        return type.cast(Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{type},
            (proxy, method, arguments) -> {
                if (method.getName().equals("toString")) {
                    return "Stub" + type.getSimpleName();
                }
                return call.invoke(
                    method.getName(),
                    arguments == null ? new Object[0] : arguments
                );
            }
        ));
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

    @FunctionalInterface
    private interface RepositoryCall {
        Object invoke(String method, Object[] arguments);
    }
}
