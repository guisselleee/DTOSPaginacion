package com.example.weill.user;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {

    private static final Pattern BASIC_EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final WeillUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(WeillUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public WeillUser register(UserRegistrationForm form) {
        String email = normalizeRequiredEmail(form.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateEmailException(email);
        }

        WeillUser user = new WeillUser(
            email,
            normalizeRequiredText(form.name(), "El nombre es obligatorio."),
            normalizeRequiredText(form.educationLevel(), "La escolaridad es obligatoria."),
            normalizeRequiredText(form.schoolCenter(), "El centro escolar es obligatorio."),
            normalizeRequiredText(form.municipality(), "El municipio es obligatorio."),
            normalizeRequiredText(form.department(), "El departamento es obligatorio."),
            Optional.ofNullable(form.birthDate())
                .orElseThrow(() -> new InvalidUserFormException("La fecha de nacimiento es obligatoria.")),
            Optional.ofNullable(form.currentlyStudying())
                .orElseThrow(() -> new InvalidUserFormException("Indica si actualmente estudia."))
        );

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException(email);
        }
    }

    @Transactional(readOnly = true)
    public Optional<WeillUser> findByEmail(String email) {
        String normalizedEmail = normalizeRequiredEmail(email);
        return userRepository.findByEmailIgnoreCaseAndRole(normalizedEmail, UserRole.EVALUADO);
    }

    @Transactional(readOnly = true)
    public Optional<WeillUser> authenticateInstitutionalUser(String email, String password) {
        String normalizedEmail = normalizeRequiredEmail(email);
        if (password == null || password.isBlank()) {
            throw new InvalidUserFormException("La contraseña es obligatoria.");
        }
        return userRepository.findByEmailIgnoreCase(normalizedEmail)
            .filter(user -> user.getRole().requiresPassword())
            .filter(user -> user.getPasswordHash() != null)
            .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()));
    }

    @Transactional
    public WeillUser saveAuthorizedUser(
        String name,
        String email,
        String password,
        UserRole role
    ) {
        if (role == null || !role.requiresPassword()) {
            throw new InvalidUserFormException("Selecciona un rol institucional válido.");
        }
        String normalizedEmail = normalizeRequiredEmail(email);
        String normalizedName = normalizeRequiredText(name, "El nombre es obligatorio.");
        WeillUser user = userRepository.findByEmailIgnoreCase(normalizedEmail)
            .orElseGet(() -> new WeillUser(
                normalizedEmail,
                normalizedName,
                passwordEncoder.encode(requirePassword(password)),
                role
            ));
        String passwordHash = password == null || password.isBlank()
            ? user.getPasswordHash()
            : passwordEncoder.encode(password);
        if (passwordHash == null) {
            throw new InvalidUserFormException("La contraseña es obligatoria.");
        }
        user.updateAuthorizedAccess(normalizedName, passwordHash, role);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public java.util.List<WeillUser> authorizedUsers() {
        return userRepository.findByRoleNotOrderByDisplayNameAsc(UserRole.EVALUADO);
    }

    @Transactional(readOnly = true)
    public WeillUser getUser(long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }

    private String normalizeRequiredEmail(String email) {
        String normalizedEmail = normalizeRequiredText(email, "El correo electronico es obligatorio.").toLowerCase();
        if (!BASIC_EMAIL.matcher(normalizedEmail).matches()) {
            throw new InvalidUserFormException("Ingresa un correo electronico valido.");
        }
        return normalizedEmail;
    }

    private String normalizeRequiredText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidUserFormException(message);
        }
        return value.trim();
    }

    private String requirePassword(String password) {
        return normalizeRequiredText(password, "La contraseña es obligatoria.");
    }
}
