package com.example.weill.user;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class AdminUserSeederTests {

    @Test
    void seedsAnAdminWithAHashedPassword() {
        AtomicReference<WeillUser> savedUser = new AtomicReference<>();
        WeillUserRepository userRepository = (WeillUserRepository) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{WeillUserRepository.class},
            (proxy, method, arguments) -> switch (method.getName()) {
                case "findByEmailIgnoreCase" -> Optional.empty();
                case "save" -> {
                    savedUser.set((WeillUser) arguments[0]);
                    yield arguments[0];
                }
                case "toString" -> "StubUserRepository";
                default -> throw new UnsupportedOperationException(method.getName());
            }
        );
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        AdminUserSeeder seeder = new AdminUserSeeder(
            userRepository,
            passwordEncoder,
            "admin@weill.local",
            "WeillAdmin2026!"
        );
        seeder.run(null);

        WeillUser savedAdmin = savedUser.get();
        assertThat(savedAdmin.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(savedAdmin.getEmail()).isEqualTo("admin@weill.local");
        assertThat(savedAdmin.getPasswordHash()).isNotEqualTo("WeillAdmin2026!");
        assertThat(passwordEncoder.matches("WeillAdmin2026!", savedAdmin.getPasswordHash())).isTrue();
    }
}
