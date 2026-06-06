package com.example.weill.user;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class UserServiceTests {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void authenticatesAnAdminWithTheCorrectPassword() {
        WeillUser admin = new WeillUser(
            "admin@weill.local",
            "Administrador Weill",
            passwordEncoder.encode("WeillAdmin2026!"),
            UserRole.ADMIN
        );
        WeillUserRepository userRepository = repositoryReturning(admin);
        UserService userService = new UserService(userRepository, passwordEncoder);

        assertThat(userService.authenticateAdmin("ADMIN@WEILL.LOCAL", "WeillAdmin2026!"))
            .contains(admin);
    }

    @Test
    void rejectsAnAdminWithAnIncorrectPassword() {
        WeillUser admin = new WeillUser(
            "admin@weill.local",
            "Administrador Weill",
            passwordEncoder.encode("WeillAdmin2026!"),
            UserRole.ADMIN
        );
        WeillUserRepository userRepository = repositoryReturning(admin);
        UserService userService = new UserService(userRepository, passwordEncoder);

        assertThat(userService.authenticateAdmin("admin@weill.local", "incorrecta")).isEmpty();
    }

    @Test
    void regularLoginOnlyLooksUpRegularUsers() {
        AtomicReference<UserRole> requestedRole = new AtomicReference<>();
        WeillUserRepository userRepository = repository((methodName, arguments) -> {
            if (methodName.equals("findByEmailIgnoreCaseAndRole")) {
                requestedRole.set((UserRole) arguments[1]);
                return Optional.empty();
            }
            return defaultValue(methodName);
        });
        UserService userService = new UserService(userRepository, passwordEncoder);

        assertThat(userService.findByEmail("USER@EXAMPLE.COM")).isEmpty();
        assertThat(requestedRole).hasValue(UserRole.USER);
    }

    private WeillUserRepository repositoryReturning(WeillUser user) {
        return repository((methodName, arguments) -> {
            if (methodName.equals("findByEmailIgnoreCaseAndRole")) {
                return Optional.of(user);
            }
            return defaultValue(methodName);
        });
    }

    private WeillUserRepository repository(RepositoryCall call) {
        return (WeillUserRepository) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{WeillUserRepository.class},
            (proxy, method, arguments) -> call.invoke(method.getName(), arguments == null ? new Object[0] : arguments)
        );
    }

    private Object defaultValue(String methodName) {
        if (methodName.equals("toString")) {
            return "StubUserRepository";
        }
        return Optional.empty();
    }

    @FunctionalInterface
    private interface RepositoryCall {
        Object invoke(String methodName, Object[] arguments);
    }
}
