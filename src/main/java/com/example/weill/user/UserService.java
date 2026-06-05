package com.example.weill.user;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {

    private static final Pattern BASIC_EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final WeillUserRepository userRepository;

    public UserService(WeillUserRepository userRepository) {
        this.userRepository = userRepository;
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
        return userRepository.findByEmailIgnoreCase(normalizedEmail);
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

}
