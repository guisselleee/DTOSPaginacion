package com.example.weill.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminUserSeeder implements ApplicationRunner {

    private final WeillUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminUserSeeder(
        WeillUserRepository userRepository,
        PasswordEncoder passwordEncoder,
        @Value("${weill.admin.email:admin@weill.local}") String adminEmail,
        @Value("${weill.admin.password:WeillAdmin2026!}") String adminPassword
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail.trim().toLowerCase();
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.findByEmailIgnoreCase(adminEmail).isPresent()) {
            return;
        }

        userRepository.save(new WeillUser(
            adminEmail,
            "Administrador Weill",
            passwordEncoder.encode(adminPassword),
            UserRole.ADMIN
        ));
    }
}
