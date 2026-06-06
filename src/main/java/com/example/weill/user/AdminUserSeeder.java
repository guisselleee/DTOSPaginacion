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
    private final String deanEmail;
    private final String deanPassword;
    private final String technicalAdminEmail;
    private final String technicalAdminPassword;

    public AdminUserSeeder(
        WeillUserRepository userRepository,
        PasswordEncoder passwordEncoder,
        @Value("${weill.admin.email:admin@weill.local}") String adminEmail,
        @Value("${weill.admin.password:WeillAdmin2026!}") String adminPassword,
        @Value("${weill.dean.email:decana@weill.local}") String deanEmail,
        @Value("${weill.dean.password:WeillDecana2026!}") String deanPassword,
        @Value("${weill.technical-admin.email:tecnico@weill.local}") String technicalAdminEmail,
        @Value("${weill.technical-admin.password:WeillTecnico2026!}") String technicalAdminPassword
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail.trim().toLowerCase();
        this.adminPassword = adminPassword;
        this.deanEmail = deanEmail.trim().toLowerCase();
        this.deanPassword = deanPassword;
        this.technicalAdminEmail = technicalAdminEmail.trim().toLowerCase();
        this.technicalAdminPassword = technicalAdminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seed(adminEmail, "Licenciado Weill", adminPassword, UserRole.LICENCIADO);
        seed(deanEmail, "Decana Weill", deanPassword, UserRole.DECANA);
        seed(
            technicalAdminEmail,
            "Administrador Técnico Weill",
            technicalAdminPassword,
            UserRole.ADMIN_TECNICO
        );
    }

    private void seed(String email, String name, String password, UserRole role) {
        if (userRepository.findByEmailIgnoreCase(email).isEmpty()) {
            userRepository.save(new WeillUser(
                email,
                name,
                passwordEncoder.encode(password),
                role
            ));
        }
    }
}
